/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoClientException;

public class MongoWaitQueueFullException
extends MongoClientException {
    private static final long serialVersionUID = 1482094507852255793L;

    public MongoWaitQueueFullException(String message) {
        super(message);
    }
}

