/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.operation.BatchCursor;
import com.mongodb.operation.MapReduceStatistics;

@Deprecated
public interface MapReduceBatchCursor<T>
extends BatchCursor<T> {
    public MapReduceStatistics getStatistics();
}

