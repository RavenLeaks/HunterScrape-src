/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.ReadConcernLevel;
import com.mongodb.assertions.Assertions;
import com.mongodb.lang.Nullable;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

public final class ReadConcern {
    private final ReadConcernLevel level;
    public static final ReadConcern DEFAULT = new ReadConcern();
    public static final ReadConcern LOCAL = new ReadConcern(ReadConcernLevel.LOCAL);
    public static final ReadConcern MAJORITY = new ReadConcern(ReadConcernLevel.MAJORITY);
    public static final ReadConcern LINEARIZABLE = new ReadConcern(ReadConcernLevel.LINEARIZABLE);
    public static final ReadConcern SNAPSHOT = new ReadConcern(ReadConcernLevel.SNAPSHOT);
    public static final ReadConcern AVAILABLE = new ReadConcern(ReadConcernLevel.AVAILABLE);

    public ReadConcern(ReadConcernLevel level) {
        this.level = Assertions.notNull("level", level);
    }

    @Nullable
    public ReadConcernLevel getLevel() {
        return this.level;
    }

    public boolean isServerDefault() {
        return this.level == null;
    }

    public BsonDocument asDocument() {
        BsonDocument readConcern = new BsonDocument();
        if (this.level != null) {
            readConcern.put("level", new BsonString(this.level.getValue()));
        }
        return readConcern;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ReadConcern that = (ReadConcern)o;
        return this.level == that.level;
    }

    public int hashCode() {
        return this.level != null ? this.level.hashCode() : 0;
    }

    private ReadConcern() {
        this.level = null;
    }
}

