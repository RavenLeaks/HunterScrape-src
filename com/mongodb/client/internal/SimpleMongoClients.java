/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.SimpleMongoClient;

final class SimpleMongoClients {
    static SimpleMongoClient create(MongoClient mongoClient) {
        return new SimpleMongoClient(){

            @Override
            public MongoDatabase getDatabase(String databaseName) {
                return MongoClient.this.getDatabase(databaseName);
            }

            @Override
            public void close() {
                MongoClient.this.close();
            }
        };
    }

    private SimpleMongoClients() {
    }

}

