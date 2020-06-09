/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.binding.AsyncWriteBinding;

@Deprecated
public interface AsyncWriteOperation<T> {
    public void executeAsync(AsyncWriteBinding var1, SingleResultCallback<T> var2);
}

