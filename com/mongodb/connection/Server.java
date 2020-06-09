/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import com.mongodb.annotations.ThreadSafe;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.connection.AsyncConnection;
import com.mongodb.connection.Connection;
import com.mongodb.connection.ServerDescription;

@ThreadSafe
@Deprecated
public interface Server {
    public ServerDescription getDescription();

    public Connection getConnection();

    public void getConnectionAsync(SingleResultCallback<AsyncConnection> var1);
}

