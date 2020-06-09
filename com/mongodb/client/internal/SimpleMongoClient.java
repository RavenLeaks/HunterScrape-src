/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.client.MongoDatabase;
import java.io.Closeable;

public interface SimpleMongoClient
extends Closeable {
    public MongoDatabase getDatabase(String var1);

    @Override
    public void close();
}

