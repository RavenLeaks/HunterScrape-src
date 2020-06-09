/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.connection.StreamFactory;

public interface StreamFactoryFactory {
    public StreamFactory create(SocketSettings var1, SslSettings var2);
}

