/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.ServerDescription;
import com.mongodb.internal.connection.ChangeListener;
import com.mongodb.internal.connection.ServerMonitor;

interface ServerMonitorFactory {
    public ServerMonitor create(ChangeListener<ServerDescription> var1);
}

