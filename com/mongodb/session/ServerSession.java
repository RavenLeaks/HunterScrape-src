/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.session;

import org.bson.BsonDocument;

public interface ServerSession {
    public BsonDocument getIdentifier();

    public long getTransactionNumber();

    public long advanceTransactionNumber();

    public boolean isClosed();
}

