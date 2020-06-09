/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

interface ServerMonitor {
    public void start();

    public void connect();

    public void close();
}

