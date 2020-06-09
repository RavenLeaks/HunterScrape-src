/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBCollection;

abstract class WriteRequest {
    WriteRequest() {
    }

    abstract com.mongodb.bulk.WriteRequest toNew(DBCollection var1);
}

