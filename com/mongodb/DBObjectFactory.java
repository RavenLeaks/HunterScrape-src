/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBObject;
import java.util.List;

interface DBObjectFactory {
    public DBObject getInstance();

    public DBObject getInstance(List<String> var1);
}

