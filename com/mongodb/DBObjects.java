/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;

final class DBObjects {
    public static DBObject toDBObject(BsonDocument document) {
        return (DBObject)MongoClient.getDefaultCodecRegistry().get(DBObject.class).decode(new BsonDocumentReader(document), DecoderContext.builder().build());
    }

    private DBObjects() {
    }
}

