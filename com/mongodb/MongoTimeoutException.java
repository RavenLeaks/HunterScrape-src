/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoClientException;

public class MongoTimeoutException
extends MongoClientException {
    private static final long serialVersionUID = -3016560214331826577L;

    public MongoTimeoutException(String message) {
        super(message);
    }
}

