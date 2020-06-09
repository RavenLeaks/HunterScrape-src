/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.AsyncCompletionHandler;
import java.nio.ByteBuffer;

interface AsyncWritableByteChannel {
    public void write(ByteBuffer var1, AsyncCompletionHandler<Void> var2);
}

