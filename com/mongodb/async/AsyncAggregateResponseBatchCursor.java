/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.async;

import com.mongodb.async.AsyncBatchCursor;
import org.bson.BsonDocument;
import org.bson.BsonTimestamp;

@Deprecated
public interface AsyncAggregateResponseBatchCursor<T>
extends AsyncBatchCursor<T> {
    public BsonDocument getPostBatchResumeToken();

    public BsonTimestamp getOperationTime();

    public boolean isFirstBatchEmpty();
}

