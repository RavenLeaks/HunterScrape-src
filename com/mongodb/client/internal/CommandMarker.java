/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientException;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.MongoTimeoutException;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.internal.capi.MongoCryptOptionsHelper;
import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.bson.RawBsonDocument;
import org.bson.conversions.Bson;

class CommandMarker
implements Closeable {
    private MongoClient client;
    private final ProcessBuilder processBuilder;

    CommandMarker(Map<String, Object> options) {
        String connectionString = options.containsKey("mongocryptdURI") ? (String)options.get("mongocryptdURI") : "mongodb://localhost:27020";
        if (!options.containsKey("mongocryptdBypassSpawn") || !((Boolean)options.get("mongocryptdBypassSpawn")).booleanValue()) {
            this.processBuilder = new ProcessBuilder(MongoCryptOptionsHelper.createMongocryptdSpawnArgs(options));
            this.startProcess();
        } else {
            this.processBuilder = null;
        }
        this.client = MongoClients.create(MongoClientSettings.builder().applyConnectionString(new ConnectionString(connectionString)).applyToClusterSettings(new Block<ClusterSettings.Builder>(){

            @Override
            public void apply(ClusterSettings.Builder builder) {
                builder.serverSelectionTimeout(1L, TimeUnit.SECONDS);
            }
        }).build());
    }

    RawBsonDocument mark(String databaseName, RawBsonDocument command) {
        try {
            try {
                return this.executeCommand(databaseName, command);
            }
            catch (MongoTimeoutException e) {
                if (this.processBuilder == null) {
                    throw e;
                }
                this.startProcess();
                return this.executeCommand(databaseName, command);
            }
        }
        catch (MongoException e) {
            throw this.wrapInClientException(e);
        }
    }

    @Override
    public void close() {
        this.client.close();
    }

    private RawBsonDocument executeCommand(String databaseName, RawBsonDocument markableCommand) {
        return this.client.getDatabase(databaseName).withReadConcern(ReadConcern.DEFAULT).withReadPreference(ReadPreference.primary()).runCommand((Bson)markableCommand, RawBsonDocument.class);
    }

    private void startProcess() {
        try {
            this.processBuilder.start();
        }
        catch (IOException e) {
            throw new MongoClientException("Exception starting mongocryptd process. Is `mongocryptd` on the system path?", e);
        }
    }

    private MongoClientException wrapInClientException(MongoException e) {
        return new MongoClientException("Exception in encryption library: " + e.getMessage(), e);
    }

}

