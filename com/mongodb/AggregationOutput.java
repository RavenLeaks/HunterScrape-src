/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBObject;
import java.util.List;

@Deprecated
public class AggregationOutput {
    private final List<DBObject> results;

    AggregationOutput(List<DBObject> results) {
        this.results = results;
    }

    public Iterable<DBObject> results() {
        return this.results;
    }
}

