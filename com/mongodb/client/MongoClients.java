/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoDriverInformation;
import com.mongodb.client.MongoClient;
import com.mongodb.client.internal.MongoClientImpl;
import com.mongodb.lang.Nullable;

public final class MongoClients {
    public static MongoClient create() {
        return MongoClients.create(new ConnectionString("mongodb://localhost"));
    }

    public static MongoClient create(MongoClientSettings settings) {
        return MongoClients.create(settings, null);
    }

    public static MongoClient create(String connectionString) {
        return MongoClients.create(new ConnectionString(connectionString));
    }

    public static MongoClient create(ConnectionString connectionString) {
        return MongoClients.create(connectionString, null);
    }

    public static MongoClient create(ConnectionString connectionString, @Nullable MongoDriverInformation mongoDriverInformation) {
        return MongoClients.create(MongoClientSettings.builder().applyConnectionString(connectionString).build(), mongoDriverInformation);
    }

    public static MongoClient create(MongoClientSettings settings, @Nullable MongoDriverInformation mongoDriverInformation) {
        MongoDriverInformation.Builder builder = mongoDriverInformation == null ? MongoDriverInformation.builder() : MongoDriverInformation.builder(mongoDriverInformation);
        return new MongoClientImpl(settings, builder.driverName("sync").build());
    }

    private MongoClients() {
    }
}

