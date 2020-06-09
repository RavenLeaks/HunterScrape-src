/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.BSONTimestampCodec;
import com.mongodb.DBObject;
import com.mongodb.DBObjectCodec;
import com.mongodb.assertions.Assertions;
import java.util.Date;
import java.util.List;
import org.bson.codecs.BsonTypeClassMap;
import org.bson.codecs.Codec;
import org.bson.codecs.DateCodec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.types.BSONTimestamp;

public class DBObjectCodecProvider
implements CodecProvider {
    private final BsonTypeClassMap bsonTypeClassMap;

    public DBObjectCodecProvider() {
        this(DBObjectCodec.getDefaultBsonTypeClassMap());
    }

    public DBObjectCodecProvider(BsonTypeClassMap bsonTypeClassMap) {
        this.bsonTypeClassMap = Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap);
    }

    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == BSONTimestamp.class) {
            return new BSONTimestampCodec();
        }
        if (DBObject.class.isAssignableFrom(clazz) && !List.class.isAssignableFrom(clazz)) {
            return new DBObjectCodec(registry, this.bsonTypeClassMap);
        }
        if (Date.class.isAssignableFrom(clazz)) {
            return new DateCodec();
        }
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    public int hashCode() {
        return this.getClass().hashCode();
    }
}

