/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoException;

public class MongoGridFSException
extends MongoException {
    private static final long serialVersionUID = -3894346172927543978L;

    public MongoGridFSException(String message) {
        super(message);
    }

    public MongoGridFSException(String message, Throwable t) {
        super(message, t);
    }
}

