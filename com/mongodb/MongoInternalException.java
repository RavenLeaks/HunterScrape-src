/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoException;

public class MongoInternalException
extends MongoException {
    private static final long serialVersionUID = -4415279469780082174L;

    public MongoInternalException(String msg) {
        super(msg);
    }

    public MongoInternalException(String msg, Throwable t) {
        super(msg, t);
    }
}

