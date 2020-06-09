/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.MongoException;

public class MongoInterruptedException
extends MongoException {
    private static final long serialVersionUID = -4110417867718417860L;

    public MongoInterruptedException(String message, Exception e) {
        super(message, e);
    }
}

