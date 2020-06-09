/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.bulk;

import com.mongodb.WriteError;
import org.bson.BsonDocument;

public class BulkWriteError
extends WriteError {
    private final int index;

    public BulkWriteError(int code, String message, BsonDocument details, int index) {
        super(code, message, details);
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BulkWriteError that = (BulkWriteError)o;
        if (this.index != that.index) {
            return false;
        }
        return super.equals(that);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.index;
        return result;
    }

    @Override
    public String toString() {
        return "BulkWriteError{index=" + this.index + ", code=" + this.getCode() + ", message='" + this.getMessage() + '\'' + ", details=" + this.getDetails() + '}';
    }
}

