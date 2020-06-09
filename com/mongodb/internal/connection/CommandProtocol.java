/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.async.SingleResultCallback;
import com.mongodb.internal.connection.InternalConnection;
import com.mongodb.session.SessionContext;

public interface CommandProtocol<T> {
    public T execute(InternalConnection var1);

    public void executeAsync(InternalConnection var1, SingleResultCallback<T> var2);

    public CommandProtocol<T> sessionContext(SessionContext var1);
}

