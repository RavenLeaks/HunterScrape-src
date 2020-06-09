/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoCommandException;
import com.mongodb.ServerAddress;
import org.bson.BsonDocument;

public class MongoNotPrimaryException
extends MongoCommandException {
    private static final long serialVersionUID = 694876345217027108L;

    public MongoNotPrimaryException(BsonDocument response, ServerAddress serverAddress) {
        super(response, serverAddress);
    }

    @Deprecated
    public MongoNotPrimaryException(ServerAddress serverAddress) {
        super(new BsonDocument(), serverAddress);
    }
}

