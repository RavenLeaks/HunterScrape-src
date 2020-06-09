/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.AcknowledgedBulkWriteResult;
import com.mongodb.BulkWriteException;
import com.mongodb.DBObject;
import com.mongodb.DBObjects;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.ServerAddress;
import com.mongodb.UnacknowledgedBulkWriteResult;
import com.mongodb.WriteConcernError;
import com.mongodb.bulk.BulkWriteError;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.bulk.BulkWriteUpsert;
import java.util.ArrayList;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonDocumentReader;
import org.bson.BsonReader;
import org.bson.BsonValue;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;

final class BulkWriteHelper {
    static com.mongodb.BulkWriteResult translateBulkWriteResult(BulkWriteResult bulkWriteResult, Decoder<DBObject> decoder) {
        if (bulkWriteResult.wasAcknowledged()) {
            return new AcknowledgedBulkWriteResult(bulkWriteResult.getInsertedCount(), bulkWriteResult.getMatchedCount(), bulkWriteResult.getDeletedCount(), bulkWriteResult.getModifiedCount(), BulkWriteHelper.translateBulkWriteUpserts(bulkWriteResult.getUpserts(), decoder));
        }
        return new UnacknowledgedBulkWriteResult();
    }

    static List<com.mongodb.BulkWriteUpsert> translateBulkWriteUpserts(List<BulkWriteUpsert> upserts, Decoder<DBObject> decoder) {
        ArrayList<com.mongodb.BulkWriteUpsert> retVal = new ArrayList<com.mongodb.BulkWriteUpsert>(upserts.size());
        for (BulkWriteUpsert cur : upserts) {
            retVal.add(new com.mongodb.BulkWriteUpsert(cur.getIndex(), BulkWriteHelper.getUpsertedId(cur, decoder)));
        }
        return retVal;
    }

    private static Object getUpsertedId(BulkWriteUpsert cur, Decoder<DBObject> decoder) {
        return decoder.decode(new BsonDocumentReader(new BsonDocument("_id", cur.getId())), DecoderContext.builder().build()).get("_id");
    }

    static BulkWriteException translateBulkWriteException(MongoBulkWriteException e, Decoder<DBObject> decoder) {
        return new BulkWriteException(BulkWriteHelper.translateBulkWriteResult(e.getWriteResult(), decoder), BulkWriteHelper.translateWriteErrors(e.getWriteErrors()), BulkWriteHelper.translateWriteConcernError(e.getWriteConcernError()), e.getServerAddress());
    }

    static WriteConcernError translateWriteConcernError(com.mongodb.bulk.WriteConcernError writeConcernError) {
        return writeConcernError == null ? null : new WriteConcernError(writeConcernError.getCode(), writeConcernError.getMessage(), DBObjects.toDBObject(writeConcernError.getDetails()));
    }

    static List<com.mongodb.BulkWriteError> translateWriteErrors(List<BulkWriteError> errors) {
        ArrayList<com.mongodb.BulkWriteError> retVal = new ArrayList<com.mongodb.BulkWriteError>(errors.size());
        for (BulkWriteError cur : errors) {
            retVal.add(new com.mongodb.BulkWriteError(cur.getCode(), cur.getMessage(), DBObjects.toDBObject(cur.getDetails()), cur.getIndex()));
        }
        return retVal;
    }

    private BulkWriteHelper() {
    }
}

