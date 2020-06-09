/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import org.bson.BSONObject;
import org.bson.io.OutputBuffer;

public interface DBEncoder {
    public int writeObject(OutputBuffer var1, BSONObject var2);
}

