/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.lang.Nullable;
import org.bson.BsonValue;

public abstract class WriteConcernResult {
    public abstract boolean wasAcknowledged();

    public abstract int getCount();

    public abstract boolean isUpdateOfExisting();

    @Nullable
    public abstract BsonValue getUpsertedId();

    public static WriteConcernResult acknowledged(int count, final boolean isUpdateOfExisting, final @Nullable BsonValue upsertedId) {
        return new WriteConcernResult(){

            @Override
            public boolean wasAcknowledged() {
                return true;
            }

            @Override
            public int getCount() {
                return val$count;
            }

            @Override
            public boolean isUpdateOfExisting() {
                return isUpdateOfExisting;
            }

            @Nullable
            @Override
            public BsonValue getUpsertedId() {
                return upsertedId;
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                WriteConcernResult that = (WriteConcernResult)o;
                if (!that.wasAcknowledged()) {
                    return false;
                }
                if (val$count != that.getCount()) {
                    return false;
                }
                if (isUpdateOfExisting != that.isUpdateOfExisting()) {
                    return false;
                }
                return !(upsertedId != null ? !upsertedId.equals(that.getUpsertedId()) : that.getUpsertedId() != null);
            }

            public int hashCode() {
                int result = val$count;
                result = 31 * result + (isUpdateOfExisting ? 1 : 0);
                result = 31 * result + (upsertedId != null ? upsertedId.hashCode() : 0);
                return result;
            }

            public String toString() {
                return "AcknowledgedWriteResult{count=" + val$count + ", isUpdateOfExisting=" + isUpdateOfExisting + ", upsertedId=" + upsertedId + '}';
            }
        };
    }

    public static WriteConcernResult unacknowledged() {
        return new WriteConcernResult(){

            @Override
            public boolean wasAcknowledged() {
                return false;
            }

            @Override
            public int getCount() {
                throw this.getUnacknowledgedWriteException();
            }

            @Override
            public boolean isUpdateOfExisting() {
                throw this.getUnacknowledgedWriteException();
            }

            @Override
            public BsonValue getUpsertedId() {
                throw this.getUnacknowledgedWriteException();
            }

            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                WriteConcernResult that = (WriteConcernResult)o;
                return !that.wasAcknowledged();
            }

            public int hashCode() {
                return 1;
            }

            public String toString() {
                return "UnacknowledgedWriteResult{}";
            }

            private UnsupportedOperationException getUnacknowledgedWriteException() {
                return new UnsupportedOperationException("Cannot get information about an unacknowledged write");
            }
        };
    }

}

