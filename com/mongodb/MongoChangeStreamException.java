/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoException;

public class MongoChangeStreamException
extends MongoException {
    private static final long serialVersionUID = 3621370414132219001L;

    public MongoChangeStreamException(String message) {
        super(message);
    }
}

