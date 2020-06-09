/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class DaemonThreadFactory
implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    public DaemonThreadFactory(String prefix) {
        this.namePrefix = prefix + "-" + POOL_NUMBER.getAndIncrement() + "-thread-";
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable, this.namePrefix + this.threadNumber.getAndIncrement());
        t.setDaemon(true);
        return t;
    }
}

