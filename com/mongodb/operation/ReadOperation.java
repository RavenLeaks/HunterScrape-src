/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.binding.ReadBinding;

@Deprecated
public interface ReadOperation<T> {
    public T execute(ReadBinding var1);
}

