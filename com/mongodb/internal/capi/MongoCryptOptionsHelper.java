/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  com.mongodb.crypt.capi.MongoAwsKmsProviderOptions
 *  com.mongodb.crypt.capi.MongoAwsKmsProviderOptions$Builder
 *  com.mongodb.crypt.capi.MongoCryptOptions
 *  com.mongodb.crypt.capi.MongoCryptOptions$Builder
 *  com.mongodb.crypt.capi.MongoLocalKmsProviderOptions
 *  com.mongodb.crypt.capi.MongoLocalKmsProviderOptions$Builder
 */
package com.mongodb.internal.capi;

import com.mongodb.MongoClientException;
import com.mongodb.crypt.capi.MongoAwsKmsProviderOptions;
import com.mongodb.crypt.capi.MongoCryptOptions;
import com.mongodb.crypt.capi.MongoLocalKmsProviderOptions;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bson.BsonDocument;

public final class MongoCryptOptionsHelper {
    public static MongoCryptOptions createMongoCryptOptions(Map<String, Map<String, Object>> kmsProviders, Map<String, BsonDocument> namespaceToLocalSchemaDocumentMap) {
        MongoCryptOptions.Builder mongoCryptOptionsBuilder = MongoCryptOptions.builder();
        for (Map.Entry<String, Map<String, Object>> entry : kmsProviders.entrySet()) {
            if (entry.getKey().equals("aws")) {
                mongoCryptOptionsBuilder.awsKmsProviderOptions(MongoAwsKmsProviderOptions.builder().accessKeyId((String)entry.getValue().get("accessKeyId")).secretAccessKey((String)entry.getValue().get("secretAccessKey")).build());
                continue;
            }
            if (entry.getKey().equals("local")) {
                mongoCryptOptionsBuilder.localKmsProviderOptions(MongoLocalKmsProviderOptions.builder().localMasterKey(ByteBuffer.wrap((byte[])entry.getValue().get("key"))).build());
                continue;
            }
            throw new MongoClientException("Unrecognized KMS provider key: " + entry.getKey());
        }
        mongoCryptOptionsBuilder.localSchemaMap(namespaceToLocalSchemaDocumentMap);
        return mongoCryptOptionsBuilder.build();
    }

    public static List<String> createMongocryptdSpawnArgs(Map<String, Object> options) {
        ArrayList<String> spawnArgs = new ArrayList<String>();
        String path = options.containsKey("mongocryptdSpawnPath") ? (String)options.get("mongocryptdSpawnPath") : "mongocryptd";
        spawnArgs.add(path);
        if (options.containsKey("mongocryptdSpawnArgs")) {
            spawnArgs.addAll((List)options.get("mongocryptdSpawnArgs"));
        }
        if (!spawnArgs.contains("--idleShutdownTimeoutSecs")) {
            spawnArgs.add("--idleShutdownTimeoutSecs");
            spawnArgs.add("60");
        }
        return spawnArgs;
    }

    private MongoCryptOptionsHelper() {
    }
}

