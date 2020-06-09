/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.BsonObjectId;
import org.bson.codecs.pojo.IdGenerator;
import org.bson.types.ObjectId;

public final class IdGenerators {
    public static final IdGenerator<ObjectId> OBJECT_ID_GENERATOR = new IdGenerator<ObjectId>(){

        @Override
        public ObjectId generate() {
            return new ObjectId();
        }

        @Override
        public Class<ObjectId> getType() {
            return ObjectId.class;
        }
    };
    public static final IdGenerator<BsonObjectId> BSON_OBJECT_ID_GENERATOR = new IdGenerator<BsonObjectId>(){

        @Override
        public BsonObjectId generate() {
            return new BsonObjectId();
        }

        @Override
        public Class<BsonObjectId> getType() {
            return BsonObjectId.class;
        }
    };

    private IdGenerators() {
    }

}

