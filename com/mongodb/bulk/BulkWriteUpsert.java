/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.bulk;

import org.bson.BsonValue;

public class BulkWriteUpsert {
    private final int index;
    private final BsonValue id;

    public BulkWriteUpsert(int index, BsonValue id) {
        this.index = index;
        this.id = id;
    }

    public int getIndex() {
        return this.index;
    }

    public BsonValue getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BulkWriteUpsert that = (BulkWriteUpsert)o;
        if (this.index != that.index) {
            return false;
        }
        return this.id.equals(that.id);
    }

    public int hashCode() {
        int result = this.index;
        result = 31 * result + this.id.hashCode();
        return result;
    }

    public String toString() {
        return "BulkWriteUpsert{index=" + this.index + ", id=" + this.id + '}';
    }
}

