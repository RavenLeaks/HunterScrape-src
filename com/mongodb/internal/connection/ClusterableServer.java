/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.Server;

interface ClusterableServer
extends Server {
    public void invalidate();

    public void invalidate(Throwable var1);

    public void close();

    public boolean isClosed();

    public void connect();
}

