/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  io.netty.bootstrap.AbstractBootstrap
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.ByteBufAllocator
 *  io.netty.buffer.CompositeByteBuf
 *  io.netty.buffer.PooledByteBufAllocator
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelFutureListener
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.SimpleChannelInboundHandler
 *  io.netty.channel.socket.SocketChannel
 *  io.netty.handler.ssl.SslHandler
 *  io.netty.handler.timeout.ReadTimeoutException
 *  io.netty.util.concurrent.EventExecutor
 *  io.netty.util.concurrent.Future
 *  io.netty.util.concurrent.GenericFutureListener
 */
package com.mongodb.connection.netty;

import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoSocketException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadTimeoutException;
import com.mongodb.ServerAddress;
import com.mongodb.connection.AsyncCompletionHandler;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.connection.Stream;
import com.mongodb.connection.netty.NettyByteBuf;
import com.mongodb.connection.netty.ReadTimeoutHandler;
import com.mongodb.internal.connection.SslHelper;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.bson.ByteBuf;

final class NettyStream
implements Stream {
    private static final String READ_HANDLER_NAME = "ReadTimeoutHandler";
    private final ServerAddress address;
    private final SocketSettings settings;
    private final SslSettings sslSettings;
    private final EventLoopGroup workerGroup;
    private final Class<? extends SocketChannel> socketChannelClass;
    private final ByteBufAllocator allocator;
    private volatile boolean isClosed;
    private volatile Channel channel;
    private final LinkedList<io.netty.buffer.ByteBuf> pendingInboundBuffers = new LinkedList();
    private volatile PendingReader pendingReader;
    private volatile Throwable pendingException;

    NettyStream(ServerAddress address, SocketSettings settings, SslSettings sslSettings, EventLoopGroup workerGroup, Class<? extends SocketChannel> socketChannelClass, ByteBufAllocator allocator) {
        this.address = address;
        this.settings = settings;
        this.sslSettings = sslSettings;
        this.workerGroup = workerGroup;
        this.socketChannelClass = socketChannelClass;
        this.allocator = allocator;
    }

    @Override
    public ByteBuf getBuffer(int size) {
        return new NettyByteBuf(this.allocator.buffer(size, size));
    }

    @Override
    public void open() throws IOException {
        FutureAsyncCompletionHandler<Void> handler = new FutureAsyncCompletionHandler<Void>();
        this.openAsync(handler);
        handler.get();
    }

    @Override
    public void openAsync(AsyncCompletionHandler<Void> handler) {
        this.initializeChannel(handler, new LinkedList<SocketAddress>(this.address.getSocketAddresses()));
    }

    private void initializeChannel(AsyncCompletionHandler<Void> handler, Queue<SocketAddress> socketAddressQueue) {
        if (socketAddressQueue.isEmpty()) {
            handler.failed(new MongoSocketException("Exception opening socket", this.getAddress()));
        } else {
            SocketAddress nextAddress = socketAddressQueue.poll();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(this.workerGroup);
            bootstrap.channel(this.socketChannelClass);
            bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (Object)this.settings.getConnectTimeout(TimeUnit.MILLISECONDS));
            bootstrap.option(ChannelOption.TCP_NODELAY, (Object)true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, (Object)this.settings.isKeepAlive());
            if (this.settings.getReceiveBufferSize() > 0) {
                bootstrap.option(ChannelOption.SO_RCVBUF, (Object)this.settings.getReceiveBufferSize());
            }
            if (this.settings.getSendBufferSize() > 0) {
                bootstrap.option(ChannelOption.SO_SNDBUF, (Object)this.settings.getSendBufferSize());
            }
            bootstrap.option(ChannelOption.ALLOCATOR, (Object)this.allocator);
            bootstrap.handler((ChannelHandler)new ChannelInitializer<SocketChannel>(){

                public void initChannel(SocketChannel ch) {
                    int readTimeout;
                    if (NettyStream.this.sslSettings.isEnabled()) {
                        SSLEngine engine = NettyStream.this.getSslContext().createSSLEngine(NettyStream.this.address.getHost(), NettyStream.this.address.getPort());
                        engine.setUseClientMode(true);
                        SSLParameters sslParameters = engine.getSSLParameters();
                        SslHelper.enableSni(NettyStream.this.address.getHost(), sslParameters);
                        if (!NettyStream.this.sslSettings.isInvalidHostNameAllowed()) {
                            SslHelper.enableHostNameVerification(sslParameters);
                        }
                        engine.setSSLParameters(sslParameters);
                        ch.pipeline().addFirst("ssl", (ChannelHandler)new SslHandler(engine, false));
                    }
                    if ((readTimeout = NettyStream.this.settings.getReadTimeout(TimeUnit.MILLISECONDS)) > 0) {
                        ch.pipeline().addLast(NettyStream.READ_HANDLER_NAME, (ChannelHandler)new ReadTimeoutHandler(readTimeout));
                    }
                    ch.pipeline().addLast(new ChannelHandler[]{new InboundBufferHandler()});
                }
            });
            ChannelFuture channelFuture = bootstrap.connect(nextAddress);
            channelFuture.addListener((GenericFutureListener)new OpenChannelFutureListener(socketAddressQueue, channelFuture, handler));
        }
    }

    @Override
    public void write(List<ByteBuf> buffers) throws IOException {
        FutureAsyncCompletionHandler<Void> future = new FutureAsyncCompletionHandler<Void>();
        this.writeAsync(buffers, future);
        future.get();
    }

    @Override
    public ByteBuf read(int numBytes) throws IOException {
        FutureAsyncCompletionHandler<ByteBuf> future = new FutureAsyncCompletionHandler<ByteBuf>();
        this.readAsync(numBytes, future);
        return future.get();
    }

    @Override
    public void writeAsync(List<ByteBuf> buffers, final AsyncCompletionHandler<Void> handler) {
        CompositeByteBuf composite = PooledByteBufAllocator.DEFAULT.compositeBuffer();
        for (ByteBuf cur : buffers) {
            composite.addComponent(true, ((NettyByteBuf)cur).asByteBuf());
        }
        this.channel.writeAndFlush((Object)composite).addListener((GenericFutureListener)new ChannelFutureListener(){

            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    handler.failed(future.cause());
                } else {
                    handler.completed(null);
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void readAsync(int numBytes, AsyncCompletionHandler<ByteBuf> handler) {
        this.scheduleReadTimeout();
        ByteBuf buffer = null;
        Throwable exceptionResult = null;
        NettyStream nettyStream = this;
        synchronized (nettyStream) {
            exceptionResult = this.pendingException;
            if (exceptionResult == null) {
                if (!this.hasBytesAvailable(numBytes)) {
                    this.pendingReader = new PendingReader(numBytes, handler);
                } else {
                    CompositeByteBuf composite = this.allocator.compositeBuffer(this.pendingInboundBuffers.size());
                    int bytesNeeded = numBytes;
                    Iterator iter = this.pendingInboundBuffers.iterator();
                    while (iter.hasNext()) {
                        io.netty.buffer.ByteBuf next = (io.netty.buffer.ByteBuf)iter.next();
                        int bytesNeededFromCurrentBuffer = Math.min(next.readableBytes(), bytesNeeded);
                        if (bytesNeededFromCurrentBuffer == next.readableBytes()) {
                            composite.addComponent(next);
                            iter.remove();
                        } else {
                            next.retain();
                            composite.addComponent(next.readSlice(bytesNeededFromCurrentBuffer));
                        }
                        composite.writerIndex(composite.writerIndex() + bytesNeededFromCurrentBuffer);
                        if ((bytesNeeded -= bytesNeededFromCurrentBuffer) != 0) continue;
                        break;
                    }
                    buffer = new NettyByteBuf((io.netty.buffer.ByteBuf)composite).flip();
                }
            }
        }
        if (exceptionResult != null) {
            this.disableReadTimeout();
            handler.failed(exceptionResult);
        }
        if (buffer != null) {
            this.disableReadTimeout();
            handler.completed(buffer);
        }
    }

    private boolean hasBytesAvailable(int numBytes) {
        int bytesAvailable = 0;
        for (io.netty.buffer.ByteBuf cur : this.pendingInboundBuffers) {
            if ((bytesAvailable += cur.readableBytes()) < numBytes) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleReadResponse(io.netty.buffer.ByteBuf buffer, Throwable t) {
        PendingReader localPendingReader = null;
        NettyStream nettyStream = this;
        synchronized (nettyStream) {
            if (buffer != null) {
                this.pendingInboundBuffers.add(buffer.retain());
            } else {
                this.pendingException = t;
            }
            if (this.pendingReader != null) {
                localPendingReader = this.pendingReader;
                this.pendingReader = null;
            }
        }
        if (localPendingReader != null) {
            this.readAsync(localPendingReader.numBytes, localPendingReader.handler);
        }
    }

    @Override
    public ServerAddress getAddress() {
        return this.address;
    }

    @Override
    public void close() {
        this.isClosed = true;
        if (this.channel != null) {
            this.channel.close();
            this.channel = null;
        }
        Iterator iterator = this.pendingInboundBuffers.iterator();
        while (iterator.hasNext()) {
            io.netty.buffer.ByteBuf nextByteBuf = (io.netty.buffer.ByteBuf)iterator.next();
            iterator.remove();
            nextByteBuf.release();
        }
    }

    @Override
    public boolean isClosed() {
        return this.isClosed;
    }

    public SocketSettings getSettings() {
        return this.settings;
    }

    public SslSettings getSslSettings() {
        return this.sslSettings;
    }

    public EventLoopGroup getWorkerGroup() {
        return this.workerGroup;
    }

    public Class<? extends SocketChannel> getSocketChannelClass() {
        return this.socketChannelClass;
    }

    public ByteBufAllocator getAllocator() {
        return this.allocator;
    }

    private SSLContext getSslContext() {
        try {
            return this.sslSettings.getContext() == null ? SSLContext.getDefault() : this.sslSettings.getContext();
        }
        catch (NoSuchAlgorithmException e) {
            throw new MongoClientException("Unable to create default SSLContext", e);
        }
    }

    private void scheduleReadTimeout() {
        this.adjustTimeout(false);
    }

    private void disableReadTimeout() {
        this.adjustTimeout(true);
    }

    private void adjustTimeout(boolean disable) {
        ChannelHandler timeoutHandler = this.channel.pipeline().get(READ_HANDLER_NAME);
        if (timeoutHandler != null) {
            final ReadTimeoutHandler readTimeoutHandler = (ReadTimeoutHandler)timeoutHandler;
            final ChannelHandlerContext handlerContext = this.channel.pipeline().context(timeoutHandler);
            EventExecutor executor = handlerContext.executor();
            if (disable) {
                if (executor.inEventLoop()) {
                    readTimeoutHandler.removeTimeout(handlerContext);
                } else {
                    executor.submit(new Runnable(){

                        @Override
                        public void run() {
                            readTimeoutHandler.removeTimeout(handlerContext);
                        }
                    });
                }
            } else if (executor.inEventLoop()) {
                readTimeoutHandler.scheduleTimeout(handlerContext);
            } else {
                executor.submit(new Runnable(){

                    @Override
                    public void run() {
                        readTimeoutHandler.scheduleTimeout(handlerContext);
                    }
                });
            }
        }
    }

    private class OpenChannelFutureListener
    implements ChannelFutureListener {
        private final Queue<SocketAddress> socketAddressQueue;
        private final ChannelFuture channelFuture;
        private final AsyncCompletionHandler<Void> handler;

        OpenChannelFutureListener(Queue<SocketAddress> socketAddressQueue, ChannelFuture channelFuture, AsyncCompletionHandler<Void> handler) {
            this.socketAddressQueue = socketAddressQueue;
            this.channelFuture = channelFuture;
            this.handler = handler;
        }

        public void operationComplete(ChannelFuture future) {
            if (future.isSuccess()) {
                NettyStream.this.channel = this.channelFuture.channel();
                NettyStream.this.channel.closeFuture().addListener((GenericFutureListener)new ChannelFutureListener(){

                    public void operationComplete(ChannelFuture future) {
                        NettyStream.this.handleReadResponse(null, new IOException("The connection to the server was closed"));
                    }
                });
                this.handler.completed(null);
            } else if (this.socketAddressQueue.isEmpty()) {
                this.handler.failed(new MongoSocketOpenException("Exception opening socket", NettyStream.this.getAddress(), future.cause()));
            } else {
                NettyStream.this.initializeChannel(this.handler, this.socketAddressQueue);
            }
        }

    }

    private static final class FutureAsyncCompletionHandler<T>
    implements AsyncCompletionHandler<T> {
        private final CountDownLatch latch = new CountDownLatch(1);
        private volatile T t;
        private volatile Throwable throwable;

        FutureAsyncCompletionHandler() {
        }

        @Override
        public void completed(T t) {
            this.t = t;
            this.latch.countDown();
        }

        @Override
        public void failed(Throwable t) {
            this.throwable = t;
            this.latch.countDown();
        }

        public T get() throws IOException {
            try {
                this.latch.await();
                if (this.throwable != null) {
                    if (this.throwable instanceof IOException) {
                        throw (IOException)this.throwable;
                    }
                    if (this.throwable instanceof MongoException) {
                        throw (MongoException)this.throwable;
                    }
                    throw new MongoInternalException("Exception thrown from Netty Stream", this.throwable);
                }
                return this.t;
            }
            catch (InterruptedException e) {
                throw new MongoInterruptedException("Interrupted", e);
            }
        }
    }

    private static final class PendingReader {
        private final int numBytes;
        private final AsyncCompletionHandler<ByteBuf> handler;

        private PendingReader(int numBytes, AsyncCompletionHandler<ByteBuf> handler) {
            this.numBytes = numBytes;
            this.handler = handler;
        }
    }

    private class InboundBufferHandler
    extends SimpleChannelInboundHandler<io.netty.buffer.ByteBuf> {
        private InboundBufferHandler() {
        }

        protected void channelRead0(ChannelHandlerContext ctx, io.netty.buffer.ByteBuf buffer) throws Exception {
            NettyStream.this.handleReadResponse(buffer, null);
        }

        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
            if (t instanceof ReadTimeoutException) {
                NettyStream.this.handleReadResponse(null, new MongoSocketReadTimeoutException("Timeout while receiving message", NettyStream.this.address, t));
            } else {
                NettyStream.this.handleReadResponse(null, t);
            }
            ctx.close();
        }
    }

}

