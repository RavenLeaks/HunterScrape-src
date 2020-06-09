/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.gridfs;

import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBucketImpl;

public final class GridFSBuckets {
    public static GridFSBucket create(MongoDatabase database) {
        return new GridFSBucketImpl(database);
    }

    public static GridFSBucket create(MongoDatabase database, String bucketName) {
        return new GridFSBucketImpl(database, bucketName);
    }

    private GridFSBuckets() {
    }
}

