/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection.tlschannel;

import org.bson.ByteBuf;

public interface BufferAllocator {
    public ByteBuf allocate(int var1);

    public void free(ByteBuf var1);
}

