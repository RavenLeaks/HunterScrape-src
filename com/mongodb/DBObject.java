/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import org.bson.BSONObject;

public interface DBObject
extends BSONObject {
    public void markAsPartialObject();

    public boolean isPartialObject();
}

