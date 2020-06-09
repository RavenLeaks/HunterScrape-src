/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoServerException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteError;

public class MongoWriteException
extends MongoServerException {
    private static final long serialVersionUID = -1906795074458258147L;
    private final WriteError error;

    public MongoWriteException(WriteError error, ServerAddress serverAddress) {
        super(error.getCode(), error.getMessage(), serverAddress);
        this.error = error;
    }

    public WriteError getError() {
        return this.error;
    }
}

