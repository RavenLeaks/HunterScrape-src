/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.DnsSrvRecordInitializer;
import com.mongodb.internal.connection.DnsSrvRecordMonitor;

public interface DnsSrvRecordMonitorFactory {
    public DnsSrvRecordMonitor create(String var1, DnsSrvRecordInitializer var2);
}

