/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.BulkWriteUpsert;
import java.util.List;

public abstract class BulkWriteResult {
    public abstract boolean isAcknowledged();

    public abstract int getInsertedCount();

    public abstract int getMatchedCount();

    public abstract int getRemovedCount();

    @Deprecated
    public abstract boolean isModifiedCountAvailable();

    public abstract int getModifiedCount();

    public abstract List<BulkWriteUpsert> getUpserts();
}

