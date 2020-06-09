/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.management;

public interface ConnectionPoolStatisticsMBean {
    public String getHost();

    public int getPort();

    public int getMinSize();

    public int getMaxSize();

    public int getSize();

    public int getCheckedOutCount();

    public int getWaitQueueSize();
}

