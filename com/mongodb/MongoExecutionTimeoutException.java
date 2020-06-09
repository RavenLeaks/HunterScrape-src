/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoException;

public class MongoExecutionTimeoutException
extends MongoException {
    private static final long serialVersionUID = 5955669123800274594L;

    public MongoExecutionTimeoutException(int code, String message) {
        super(code, message);
    }
}

