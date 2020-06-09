/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.annotations.NotThreadSafe;
import com.mongodb.operation.BatchCursor;
import org.bson.BsonDocument;
import org.bson.BsonTimestamp;

@NotThreadSafe
@Deprecated
public interface AggregateResponseBatchCursor<T>
extends BatchCursor<T> {
    public BsonDocument getPostBatchResumeToken();

    public BsonTimestamp getOperationTime();

    public boolean isFirstBatchEmpty();
}

