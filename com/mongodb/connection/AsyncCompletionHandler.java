/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

public interface AsyncCompletionHandler<T> {
    public void completed(T var1);

    public void failed(Throwable var1);
}

