/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.async.AsyncBatchCursor;
import com.mongodb.operation.MapReduceStatistics;

@Deprecated
public interface MapReduceAsyncBatchCursor<T>
extends AsyncBatchCursor<T> {
    public MapReduceStatistics getStatistics();
}

