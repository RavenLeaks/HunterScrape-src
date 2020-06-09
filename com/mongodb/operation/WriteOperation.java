/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.binding.WriteBinding;

@Deprecated
public interface WriteOperation<T> {
    public T execute(WriteBinding var1);
}

