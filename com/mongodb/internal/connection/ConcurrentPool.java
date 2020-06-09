/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.MongoInternalException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.internal.connection.ConcurrentLinkedDeque;
import com.mongodb.internal.connection.Pool;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class ConcurrentPool<T>
implements Pool<T> {
    private final int maxSize;
    private final ItemFactory<T> itemFactory;
    private final ConcurrentLinkedDeque<T> available = new ConcurrentLinkedDeque();
    private final Semaphore permits;
    private volatile boolean closed;

    public ConcurrentPool(int maxSize, ItemFactory<T> itemFactory) {
        this.maxSize = maxSize;
        this.itemFactory = itemFactory;
        this.permits = new Semaphore(maxSize, true);
    }

    @Override
    public void release(T t) {
        this.release(t, false);
    }

    @Override
    public void release(T t, boolean prune) {
        if (t == null) {
            throw new IllegalArgumentException("Can not return a null item to the pool");
        }
        if (this.closed) {
            this.close(t);
            return;
        }
        if (prune) {
            this.close(t);
        } else {
            this.available.addLast(t);
        }
        this.releasePermit();
    }

    @Override
    public T get() {
        return this.get(-1L, TimeUnit.MILLISECONDS);
    }

    @Override
    public T get(long timeout, TimeUnit timeUnit) {
        if (this.closed) {
            throw new IllegalStateException("The pool is closed");
        }
        if (!this.acquirePermit(timeout, timeUnit)) {
            throw new MongoTimeoutException(String.format("Timeout waiting for a pooled item after %d %s", new Object[]{timeout, timeUnit}));
        }
        T t = this.available.pollLast();
        if (t == null) {
            t = this.createNewAndReleasePermitIfFailure(false);
        }
        return t;
    }

    public void prune() {
        Object cur;
        Prune shouldPrune;
        Iterator iter = this.available.iterator();
        while (iter.hasNext() && (shouldPrune = this.itemFactory.shouldPrune(cur = iter.next())) != Prune.STOP) {
            boolean removed;
            if (shouldPrune != Prune.YES || !(removed = iter.reportingRemove())) continue;
            this.close(cur);
        }
    }

    public void ensureMinSize(int minSize, boolean initialize) {
        while (this.getCount() < minSize && this.acquirePermit(10L, TimeUnit.MILLISECONDS)) {
            this.release(this.createNewAndReleasePermitIfFailure(initialize));
        }
    }

    private T createNewAndReleasePermitIfFailure(boolean initialize) {
        try {
            T newMember = this.itemFactory.create(initialize);
            if (newMember == null) {
                throw new MongoInternalException("The factory for the pool created a null item");
            }
            return newMember;
        }
        catch (RuntimeException e) {
            this.permits.release();
            throw e;
        }
    }

    protected boolean acquirePermit(long timeout, TimeUnit timeUnit) {
        try {
            if (this.closed) {
                return false;
            }
            if (timeout >= 0L) {
                return this.permits.tryAcquire(timeout, timeUnit);
            }
            this.permits.acquire();
            return true;
        }
        catch (InterruptedException e) {
            throw new MongoInterruptedException("Interrupted acquiring a permit to retrieve an item from the pool ", e);
        }
    }

    protected void releasePermit() {
        this.permits.release();
    }

    @Override
    public void close() {
        this.closed = true;
        Iterator iter = this.available.iterator();
        while (iter.hasNext()) {
            Object t = iter.next();
            this.close(t);
            iter.remove();
        }
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public int getInUseCount() {
        return this.maxSize - this.permits.availablePermits();
    }

    public int getAvailableCount() {
        return this.available.size();
    }

    public int getCount() {
        return this.getInUseCount() + this.getAvailableCount();
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("pool: ").append(" maxSize: ").append(this.maxSize).append(" availableCount ").append(this.getAvailableCount()).append(" inUseCount ").append(this.getInUseCount());
        return buf.toString();
    }

    private void close(T t) {
        try {
            this.itemFactory.close(t);
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
    }

    public static interface ItemFactory<T> {
        public T create(boolean var1);

        public void close(T var1);

        public Prune shouldPrune(T var1);
    }

    public static enum Prune {
        YES,
        NO,
        STOP;
        
    }

}

