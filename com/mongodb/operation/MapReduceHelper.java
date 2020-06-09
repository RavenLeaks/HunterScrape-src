/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.operation.MapReduceStatistics;
import org.bson.BsonDocument;
import org.bson.BsonNumber;

final class MapReduceHelper {
    static MapReduceStatistics createStatistics(BsonDocument result) {
        return new MapReduceStatistics(MapReduceHelper.getInputCount(result), MapReduceHelper.getOutputCount(result), MapReduceHelper.getEmitCount(result), MapReduceHelper.getDuration(result));
    }

    private static int getInputCount(BsonDocument result) {
        return result.getDocument("counts").getNumber("input").intValue();
    }

    private static int getOutputCount(BsonDocument result) {
        return result.getDocument("counts").getNumber("output").intValue();
    }

    private static int getEmitCount(BsonDocument result) {
        return result.getDocument("counts").getNumber("emit").intValue();
    }

    private static int getDuration(BsonDocument result) {
        return result.getNumber("timeMillis").intValue();
    }

    private MapReduceHelper() {
    }
}

