/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import java.io.Closeable;
import java.util.Iterator;

public interface Cursor
extends Iterator<DBObject>,
Closeable {
    public long getCursorId();

    public ServerAddress getServerAddress();

    @Override
    public void close();
}

