/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.MongoNamespace;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.internal.SimpleMongoClient;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;

class KeyRetriever
implements Closeable {
    private final SimpleMongoClient client;
    private final boolean ownsClient;
    private final MongoNamespace namespace;

    KeyRetriever(SimpleMongoClient client, boolean ownsClient, MongoNamespace namespace) {
        this.client = Assertions.notNull("client", client);
        this.ownsClient = ownsClient;
        this.namespace = Assertions.notNull("namespace", namespace);
    }

    public List<BsonDocument> find(BsonDocument keyFilter) {
        return this.client.getDatabase(this.namespace.getDatabaseName()).getCollection(this.namespace.getCollectionName(), BsonDocument.class).find(keyFilter).into(new ArrayList());
    }

    @Override
    public void close() {
        if (this.ownsClient) {
            this.client.close();
        }
    }
}

