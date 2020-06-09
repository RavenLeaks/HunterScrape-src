/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import com.mongodb.ServerAddress;
import com.mongodb.connection.Stream;

public interface StreamFactory {
    public Stream create(ServerAddress var1);
}

