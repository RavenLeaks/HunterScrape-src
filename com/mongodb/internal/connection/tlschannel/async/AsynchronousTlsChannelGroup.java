/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection.tlschannel.async;

import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.internal.connection.tlschannel.NeedsReadException;
import com.mongodb.internal.connection.tlschannel.NeedsTaskException;
import com.mongodb.internal.connection.tlschannel.NeedsWriteException;
import com.mongodb.internal.connection.tlschannel.TlsChannel;
import com.mongodb.internal.connection.tlschannel.impl.ByteBufferSet;
import com.mongodb.internal.connection.tlschannel.util.Util;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.channels.ReadPendingException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ShutdownChannelGroupException;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritePendingException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.IntBinaryOperator;
import java.util.function.LongConsumer;

public class AsynchronousTlsChannelGroup {
    private static final Logger LOGGER = Loggers.getLogger("connection.tls");
    private static final int QUEUE_LENGTH_MULTIPLIER = 32;
    private static AtomicInteger globalGroupCount = new AtomicInteger();
    private final int id = globalGroupCount.getAndIncrement();
    private final AtomicBoolean loggedTaskWarning = new AtomicBoolean();
    private final Selector selector;
    final ExecutorService executor;
    private final ScheduledThreadPoolExecutor timeoutExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory(){

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, String.format("async-channel-group-%d-timeout-thread", AsynchronousTlsChannelGroup.this.id));
        }
    });
    private final Thread selectorThread = new Thread(new Runnable(){

        @Override
        public void run() {
            AsynchronousTlsChannelGroup.this.loop();
        }
    }, String.format("async-channel-group-%d-selector", this.id));
    private final ConcurrentLinkedQueue<RegisteredSocket> pendingRegistrations = new ConcurrentLinkedQueue();
    private volatile Shutdown shutdown = Shutdown.No;
    private LongAdder selectionCount = new LongAdder();
    private LongAdder startedReads = new LongAdder();
    private LongAdder startedWrites = new LongAdder();
    private LongAdder successfulReads = new LongAdder();
    private LongAdder successfulWrites = new LongAdder();
    private LongAdder failedReads = new LongAdder();
    private LongAdder failedWrites = new LongAdder();
    private LongAdder cancelledReads = new LongAdder();
    private LongAdder cancelledWrites = new LongAdder();
    private AtomicInteger currentRegistrations = new AtomicInteger();
    private LongAdder currentReads = new LongAdder();
    private LongAdder currentWrites = new LongAdder();

    public AsynchronousTlsChannelGroup(int nThreads) {
        try {
            this.selector = Selector.open();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.timeoutExecutor.setRemoveOnCancelPolicy(true);
        this.executor = new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(nThreads * 32), new ThreadFactory(){

            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, String.format("async-channel-group-%d-handler-executor", AsynchronousTlsChannelGroup.this.id));
            }
        }, new ThreadPoolExecutor.CallerRunsPolicy());
        this.selectorThread.start();
    }

    public AsynchronousTlsChannelGroup() {
        this(Runtime.getRuntime().availableProcessors());
    }

    RegisteredSocket registerSocket(TlsChannel reader, SocketChannel socketChannel) {
        if (this.shutdown != Shutdown.No) {
            throw new ShutdownChannelGroupException();
        }
        RegisteredSocket socket = new RegisteredSocket(reader, socketChannel);
        this.currentRegistrations.getAndIncrement();
        this.pendingRegistrations.add(socket);
        this.selector.wakeup();
        return socket;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean doCancelRead(RegisteredSocket socket, ReadOperation op) {
        socket.readLock.lock();
        try {
            if (op != null && socket.readOperation == op || op == null && socket.readOperation != null) {
                socket.readOperation = null;
                this.cancelledReads.increment();
                this.currentReads.decrement();
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            socket.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    boolean doCancelWrite(RegisteredSocket socket, WriteOperation op) {
        socket.writeLock.lock();
        try {
            if (op != null && socket.writeOperation == op || op == null && socket.writeOperation != null) {
                socket.writeOperation = null;
                this.cancelledWrites.increment();
                this.currentWrites.decrement();
                boolean bl = true;
                return bl;
            }
            boolean bl = false;
            return bl;
        }
        finally {
            socket.writeLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    ReadOperation startRead(final RegisteredSocket socket, ByteBufferSet buffer, long timeout, TimeUnit unit, LongConsumer onSuccess, Consumer<Throwable> onFailure) throws ReadPendingException {
        ReadOperation op;
        this.checkTerminated();
        Util.assertTrue(buffer.hasRemaining());
        this.waitForSocketRegistration(socket);
        socket.readLock.lock();
        try {
            if (socket.readOperation != null) {
                throw new ReadPendingException();
            }
            final ReadOperation finalOp = op = new ReadOperation(buffer, onSuccess, onFailure);
            socket.pendingOps.set(5);
            if (timeout != 0L) {
                op.timeoutFuture = this.timeoutExecutor.schedule(new Runnable(){

                    @Override
                    public void run() {
                        boolean success = AsynchronousTlsChannelGroup.this.doCancelRead(socket, finalOp);
                        if (success) {
                            finalOp.onFailure.accept(new InterruptedByTimeoutException());
                        }
                    }
                }, timeout, unit);
            }
            socket.readOperation = op;
        }
        finally {
            socket.readLock.unlock();
        }
        this.selector.wakeup();
        this.startedReads.increment();
        this.currentReads.increment();
        return op;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    WriteOperation startWrite(final RegisteredSocket socket, ByteBufferSet buffer, long timeout, TimeUnit unit, LongConsumer onSuccess, Consumer<Throwable> onFailure) throws WritePendingException {
        WriteOperation op;
        this.checkTerminated();
        Util.assertTrue(buffer.hasRemaining());
        this.waitForSocketRegistration(socket);
        socket.writeLock.lock();
        try {
            if (socket.writeOperation != null) {
                throw new WritePendingException();
            }
            final WriteOperation finalOp = op = new WriteOperation(buffer, onSuccess, onFailure);
            socket.pendingOps.set(5);
            if (timeout != 0L) {
                op.timeoutFuture = this.timeoutExecutor.schedule(new Runnable(){

                    @Override
                    public void run() {
                        boolean success = AsynchronousTlsChannelGroup.this.doCancelWrite(socket, finalOp);
                        if (success) {
                            finalOp.onFailure.accept(new InterruptedByTimeoutException());
                        }
                    }
                }, timeout, unit);
            }
            socket.writeOperation = op;
        }
        finally {
            socket.writeLock.unlock();
        }
        this.selector.wakeup();
        this.startedWrites.increment();
        this.currentWrites.increment();
        return op;
    }

    private void checkTerminated() {
        if (this.isTerminated()) {
            throw new ShutdownChannelGroupException();
        }
    }

    private void waitForSocketRegistration(RegisteredSocket socket) {
        try {
            socket.registered.await();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive exception aggregation
     */
    private void loop() {
        block21 : {
            while (this.shutdown == Shutdown.No || this.shutdown == Shutdown.Wait && this.currentRegistrations.intValue() > 0) {
                int c = this.selector.select();
                this.selectionCount.increment();
                if (c > 0) {
                    Iterator<SelectionKey> it = this.selector.selectedKeys().iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        try {
                            key.interestOps(0);
                        }
                        catch (CancelledKeyException e) {
                            continue;
                        }
                        RegisteredSocket socket = (RegisteredSocket)key.attachment();
                        this.processRead(socket);
                        this.processWrite(socket);
                    }
                }
                this.registerPendingSockets();
                this.processPendingInterests();
            }
            this.executor.shutdown();
            this.timeoutExecutor.shutdownNow();
            if (this.shutdown == Shutdown.Immediate) {
                for (SelectionKey key : this.selector.keys()) {
                    RegisteredSocket socket = (RegisteredSocket)key.attachment();
                    socket.close();
                }
            }
            try {
                this.selector.close();
            }
            catch (IOException e) {
                LOGGER.warn(String.format("error closing selector: %s", e.getMessage()));
            }
            break block21;
            catch (Throwable e) {
                LOGGER.error("error in selector loop", e);
                this.executor.shutdown();
                this.timeoutExecutor.shutdownNow();
                if (this.shutdown == Shutdown.Immediate) {
                    for (SelectionKey key : this.selector.keys()) {
                        RegisteredSocket socket = (RegisteredSocket)key.attachment();
                        socket.close();
                    }
                }
                try {
                    this.selector.close();
                }
                catch (IOException e2) {
                    LOGGER.warn(String.format("error closing selector: %s", e2.getMessage()));
                }
                catch (Throwable throwable) {
                    this.executor.shutdown();
                    this.timeoutExecutor.shutdownNow();
                    if (this.shutdown == Shutdown.Immediate) {
                        for (SelectionKey key : this.selector.keys()) {
                            RegisteredSocket socket = (RegisteredSocket)key.attachment();
                            socket.close();
                        }
                    }
                    try {
                        this.selector.close();
                    }
                    catch (IOException e3) {
                        LOGGER.warn(String.format("error closing selector: %s", e3.getMessage()));
                    }
                    throw throwable;
                }
            }
        }
    }

    private void processPendingInterests() {
        for (SelectionKey key : this.selector.keys()) {
            RegisteredSocket socket = (RegisteredSocket)key.attachment();
            int pending = socket.pendingOps.getAndSet(0);
            if (pending == 0) continue;
            key.interestOps(key.interestOps() | pending);
        }
    }

    private void processWrite(final RegisteredSocket socket) {
        socket.writeLock.lock();
        try {
            final WriteOperation op = socket.writeOperation;
            if (op != null) {
                this.executor.execute(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            AsynchronousTlsChannelGroup.this.doWrite(socket, op);
                        }
                        catch (Throwable e) {
                            LOGGER.error("error in operation", e);
                        }
                    }
                });
            }
        }
        finally {
            socket.writeLock.unlock();
        }
    }

    private void processRead(final RegisteredSocket socket) {
        socket.readLock.lock();
        try {
            final ReadOperation op = socket.readOperation;
            if (op != null) {
                this.executor.execute(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            AsynchronousTlsChannelGroup.this.doRead(socket, op);
                        }
                        catch (Throwable e) {
                            LOGGER.error("error in operation", e);
                        }
                    }
                });
            }
        }
        finally {
            socket.readLock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doWrite(RegisteredSocket socket, WriteOperation op) {
        socket.writeLock.lock();
        try {
            if (socket.writeOperation != op) {
                return;
            }
            try {
                long before = op.bufferSet.remaining();
                try {
                    this.writeHandlingTasks(socket, op);
                }
                catch (Throwable throwable) {
                    long c = before - op.bufferSet.remaining();
                    Util.assertTrue(c >= 0L);
                    op.consumesBytes += c;
                    throw throwable;
                }
                long c = before - op.bufferSet.remaining();
                Util.assertTrue(c >= 0L);
                op.consumesBytes += c;
                socket.writeOperation = null;
                if (op.timeoutFuture != null) {
                    op.timeoutFuture.cancel(false);
                }
                op.onSuccess.accept(op.consumesBytes);
                this.successfulWrites.increment();
                this.currentWrites.decrement();
            }
            catch (NeedsReadException e) {
                socket.pendingOps.accumulateAndGet(1, new IntBinaryOperator(){

                    @Override
                    public int applyAsInt(int a, int b) {
                        return a | b;
                    }
                });
                this.selector.wakeup();
            }
            catch (NeedsWriteException e) {
                socket.pendingOps.accumulateAndGet(4, new IntBinaryOperator(){

                    @Override
                    public int applyAsInt(int a, int b) {
                        return a | b;
                    }
                });
                this.selector.wakeup();
            }
            catch (IOException e) {
                if (socket.writeOperation == op) {
                    socket.writeOperation = null;
                }
                if (op.timeoutFuture != null) {
                    op.timeoutFuture.cancel(false);
                }
                op.onFailure.accept(e);
                this.failedWrites.increment();
                this.currentWrites.decrement();
            }
        }
        finally {
            socket.writeLock.unlock();
        }
    }

    private void writeHandlingTasks(RegisteredSocket socket, WriteOperation op) throws IOException {
        do {
            try {
                socket.tlsChannel.write(op.bufferSet.array, op.bufferSet.offset, op.bufferSet.length);
                return;
            }
            catch (NeedsTaskException e) {
                this.warnAboutNeedTask();
                e.getTask().run();
                continue;
            }
            break;
        } while (true);
    }

    private void warnAboutNeedTask() {
        if (!this.loggedTaskWarning.getAndSet(true)) {
            LOGGER.warn(String.format("caught %s; channels used in asynchronous groups should run tasks themselves; although task is being dealt with anyway, consider configuring channels properly", NeedsTaskException.class.getName()));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void doRead(RegisteredSocket socket, ReadOperation op) {
        socket.readLock.lock();
        try {
            if (socket.readOperation != op) {
                return;
            }
            try {
                Util.assertTrue(op.bufferSet.hasRemaining());
                long c = this.readHandlingTasks(socket, op);
                Util.assertTrue(c > 0L || c == -1L);
                socket.readOperation = null;
                if (op.timeoutFuture != null) {
                    op.timeoutFuture.cancel(false);
                }
                op.onSuccess.accept(c);
                this.successfulReads.increment();
                this.currentReads.decrement();
            }
            catch (NeedsReadException e) {
                socket.pendingOps.accumulateAndGet(1, new IntBinaryOperator(){

                    @Override
                    public int applyAsInt(int a, int b) {
                        return a | b;
                    }
                });
                this.selector.wakeup();
            }
            catch (NeedsWriteException e) {
                socket.pendingOps.accumulateAndGet(4, new IntBinaryOperator(){

                    @Override
                    public int applyAsInt(int a, int b) {
                        return a | b;
                    }
                });
                this.selector.wakeup();
            }
            catch (IOException e) {
                if (socket.readOperation == op) {
                    socket.readOperation = null;
                }
                if (op.timeoutFuture != null) {
                    op.timeoutFuture.cancel(false);
                }
                op.onFailure.accept(e);
                this.failedReads.increment();
                this.currentReads.decrement();
            }
        }
        finally {
            socket.readLock.unlock();
        }
    }

    private long readHandlingTasks(RegisteredSocket socket, ReadOperation op) throws IOException {
        do {
            try {
                return socket.tlsChannel.read(op.bufferSet.array, op.bufferSet.offset, op.bufferSet.length);
            }
            catch (NeedsTaskException e) {
                this.warnAboutNeedTask();
                e.getTask().run();
                continue;
            }
            break;
        } while (true);
    }

    private void registerPendingSockets() throws ClosedChannelException {
        RegisteredSocket socket;
        while ((socket = this.pendingRegistrations.poll()) != null) {
            socket.key = socket.socketChannel.register(this.selector, 0, socket);
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace(String.format("registered key: %ss", socket.key));
            }
            socket.registered.countDown();
        }
    }

    public boolean isShutdown() {
        return this.shutdown != Shutdown.No;
    }

    public void shutdown() {
        this.shutdown = Shutdown.Wait;
        this.selector.wakeup();
    }

    public void shutdownNow() {
        this.shutdown = Shutdown.Immediate;
        this.selector.wakeup();
    }

    public boolean isTerminated() {
        return this.executor.isTerminated();
    }

    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return this.executor.awaitTermination(timeout, unit);
    }

    long getSelectionCount() {
        return this.selectionCount.longValue();
    }

    public long getStartedReadCount() {
        return this.startedReads.longValue();
    }

    public long getStartedWriteCount() {
        return this.startedWrites.longValue();
    }

    public long getSuccessfulReadCount() {
        return this.successfulReads.longValue();
    }

    public long getSuccessfulWriteCount() {
        return this.successfulWrites.longValue();
    }

    public long getFailedReadCount() {
        return this.failedReads.longValue();
    }

    public long getFailedWriteCount() {
        return this.failedWrites.longValue();
    }

    public long getCancelledReadCount() {
        return this.cancelledReads.longValue();
    }

    public long getCancelledWriteCount() {
        return this.cancelledWrites.longValue();
    }

    public long getCurrentReadCount() {
        return this.currentReads.longValue();
    }

    public long getCurrentWriteCount() {
        return this.currentWrites.longValue();
    }

    public long getCurrentRegistrationCount() {
        return this.currentRegistrations.longValue();
    }

    private static enum Shutdown {
        No,
        Wait,
        Immediate;
        
    }

    static final class WriteOperation
    extends Operation {
        long consumesBytes = 0L;

        WriteOperation(ByteBufferSet bufferSet, LongConsumer onSuccess, Consumer<Throwable> onFailure) {
            super(bufferSet, onSuccess, onFailure);
        }
    }

    static final class ReadOperation
    extends Operation {
        ReadOperation(ByteBufferSet bufferSet, LongConsumer onSuccess, Consumer<Throwable> onFailure) {
            super(bufferSet, onSuccess, onFailure);
        }
    }

    private static abstract class Operation {
        final ByteBufferSet bufferSet;
        final LongConsumer onSuccess;
        final Consumer<Throwable> onFailure;
        Future<?> timeoutFuture;

        Operation(ByteBufferSet bufferSet, LongConsumer onSuccess, Consumer<Throwable> onFailure) {
            this.bufferSet = bufferSet;
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }
    }

    class RegisteredSocket {
        final TlsChannel tlsChannel;
        final SocketChannel socketChannel;
        final CountDownLatch registered = new CountDownLatch(1);
        SelectionKey key;
        final Lock readLock = new ReentrantLock();
        final Lock writeLock = new ReentrantLock();
        ReadOperation readOperation;
        WriteOperation writeOperation;
        final AtomicInteger pendingOps = new AtomicInteger();

        RegisteredSocket(TlsChannel tlsChannel, SocketChannel socketChannel) {
            this.tlsChannel = tlsChannel;
            this.socketChannel = socketChannel;
        }

        public void close() {
            AsynchronousTlsChannelGroup.this.doCancelRead(this, null);
            AsynchronousTlsChannelGroup.this.doCancelWrite(this, null);
            this.key.cancel();
            AsynchronousTlsChannelGroup.this.currentRegistrations.getAndDecrement();
            AsynchronousTlsChannelGroup.this.selector.wakeup();
        }
    }

}

