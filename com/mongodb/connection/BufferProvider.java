/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import org.bson.ByteBuf;

public interface BufferProvider {
    public ByteBuf getBuffer(int var1);
}

