/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoSocketException;
import com.mongodb.ServerAddress;

public class MongoSocketWriteException
extends MongoSocketException {
    private static final long serialVersionUID = 5088061954415484493L;

    public MongoSocketWriteException(String message, ServerAddress address, Throwable cause) {
        super(message, address, cause);
    }
}

