/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.BulkWriteResult;
import com.mongodb.BulkWriteUpsert;
import java.util.List;

class UnacknowledgedBulkWriteResult
extends BulkWriteResult {
    UnacknowledgedBulkWriteResult() {
    }

    @Override
    public boolean isAcknowledged() {
        return false;
    }

    @Override
    public int getInsertedCount() {
        throw this.getUnacknowledgedWriteException();
    }

    @Override
    public int getMatchedCount() {
        throw this.getUnacknowledgedWriteException();
    }

    @Override
    public int getRemovedCount() {
        throw this.getUnacknowledgedWriteException();
    }

    @Deprecated
    @Override
    public boolean isModifiedCountAvailable() {
        throw this.getUnacknowledgedWriteException();
    }

    @Override
    public int getModifiedCount() {
        throw this.getUnacknowledgedWriteException();
    }

    @Override
    public List<BulkWriteUpsert> getUpserts() {
        throw this.getUnacknowledgedWriteException();
    }

    private UnsupportedOperationException getUnacknowledgedWriteException() {
        return new UnsupportedOperationException("Can not get information about an unacknowledged write");
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    public int hashCode() {
        return 0;
    }

    public String toString() {
        return "UnacknowledgedBulkWriteResult{}";
    }
}

