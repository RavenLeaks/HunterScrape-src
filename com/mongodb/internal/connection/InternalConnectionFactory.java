/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.ServerId;
import com.mongodb.internal.connection.InternalConnection;

interface InternalConnectionFactory {
    public InternalConnection create(ServerId var1);
}

