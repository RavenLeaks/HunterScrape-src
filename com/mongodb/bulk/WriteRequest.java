/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.bulk;

@Deprecated
public abstract class WriteRequest {
    WriteRequest() {
    }

    public abstract Type getType();

    public static enum Type {
        INSERT,
        UPDATE,
        REPLACE,
        DELETE;
        
    }

}

