/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

@Deprecated
public class ParallelCollectionScanOptions {
    private int batchSize;

    public int getBatchSize() {
        return this.batchSize;
    }

    public ParallelCollectionScanOptions batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
}

