/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBCallback;
import com.mongodb.DBCollection;

public interface DBCallbackFactory {
    public DBCallback create(DBCollection var1);
}

