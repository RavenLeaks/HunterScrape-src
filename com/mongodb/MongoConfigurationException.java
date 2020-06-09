/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoClientException;

public class MongoConfigurationException
extends MongoClientException {
    private static final long serialVersionUID = -2343119787572079323L;

    public MongoConfigurationException(String message) {
        super(message);
    }

    public MongoConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}

