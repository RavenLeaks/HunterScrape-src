/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  com.mongodb.crypt.capi.MongoCrypt
 *  com.mongodb.crypt.capi.MongoCryptContext
 *  com.mongodb.crypt.capi.MongoCryptContext$State
 *  com.mongodb.crypt.capi.MongoCryptException
 *  com.mongodb.crypt.capi.MongoDataKeyOptions
 *  com.mongodb.crypt.capi.MongoDataKeyOptions$Builder
 *  com.mongodb.crypt.capi.MongoExplicitEncryptOptions
 *  com.mongodb.crypt.capi.MongoExplicitEncryptOptions$Builder
 *  com.mongodb.crypt.capi.MongoKeyDecryptor
 */
package com.mongodb.client.internal;

import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.MongoInternalException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.ServerAddress;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.internal.CollectionInfoRetriever;
import com.mongodb.client.internal.CommandMarker;
import com.mongodb.client.internal.KeyManagementService;
import com.mongodb.client.internal.KeyRetriever;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import com.mongodb.crypt.capi.MongoCrypt;
import com.mongodb.crypt.capi.MongoCryptContext;
import com.mongodb.crypt.capi.MongoCryptException;
import com.mongodb.crypt.capi.MongoDataKeyOptions;
import com.mongodb.crypt.capi.MongoExplicitEncryptOptions;
import com.mongodb.crypt.capi.MongoKeyDecryptor;
import com.mongodb.lang.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.RawBsonDocument;

class Crypt
implements Closeable {
    private final MongoCrypt mongoCrypt;
    private final CollectionInfoRetriever collectionInfoRetriever;
    private final CommandMarker commandMarker;
    private final KeyRetriever keyRetriever;
    private final KeyManagementService keyManagementService;
    private final boolean bypassAutoEncryption;

    Crypt(MongoCrypt mongoCrypt, KeyRetriever keyRetriever, KeyManagementService keyManagementService) {
        this(mongoCrypt, null, null, keyRetriever, keyManagementService, false);
    }

    Crypt(MongoCrypt mongoCrypt, @Nullable CollectionInfoRetriever collectionInfoRetriever, @Nullable CommandMarker commandMarker, KeyRetriever keyRetriever, KeyManagementService keyManagementService, boolean bypassAutoEncryption) {
        this.mongoCrypt = mongoCrypt;
        this.collectionInfoRetriever = collectionInfoRetriever;
        this.commandMarker = commandMarker;
        this.keyRetriever = keyRetriever;
        this.keyManagementService = keyManagementService;
        this.bypassAutoEncryption = bypassAutoEncryption;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public RawBsonDocument encrypt(String databaseName, RawBsonDocument command) {
        RawBsonDocument rawBsonDocument;
        Assertions.notNull("databaseName", databaseName);
        Assertions.notNull("command", command);
        if (this.bypassAutoEncryption) {
            return command;
        }
        MongoCryptContext encryptionContext = this.mongoCrypt.createEncryptionContext(databaseName, (BsonDocument)command);
        try {
            rawBsonDocument = this.executeStateMachine(encryptionContext, databaseName);
        }
        catch (Throwable throwable) {
            try {
                encryptionContext.close();
                throw throwable;
            }
            catch (MongoCryptException e) {
                throw this.wrapInClientException(e);
            }
        }
        encryptionContext.close();
        return rawBsonDocument;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    RawBsonDocument decrypt(RawBsonDocument commandResponse) {
        RawBsonDocument rawBsonDocument;
        Assertions.notNull("commandResponse", commandResponse);
        MongoCryptContext decryptionContext = this.mongoCrypt.createDecryptionContext((BsonDocument)commandResponse);
        try {
            rawBsonDocument = this.executeStateMachine(decryptionContext, null);
        }
        catch (Throwable throwable) {
            try {
                decryptionContext.close();
                throw throwable;
            }
            catch (MongoCryptException e) {
                throw this.wrapInClientException(e);
            }
        }
        decryptionContext.close();
        return rawBsonDocument;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    BsonDocument createDataKey(String kmsProvider, DataKeyOptions options) {
        RawBsonDocument rawBsonDocument;
        Assertions.notNull("kmsProvider", kmsProvider);
        Assertions.notNull("options", options);
        MongoCryptContext dataKeyCreationContext = this.mongoCrypt.createDataKeyContext(kmsProvider, MongoDataKeyOptions.builder().keyAltNames(options.getKeyAltNames()).masterKey(options.getMasterKey()).build());
        try {
            rawBsonDocument = this.executeStateMachine(dataKeyCreationContext, null);
        }
        catch (Throwable throwable) {
            try {
                dataKeyCreationContext.close();
                throw throwable;
            }
            catch (MongoCryptException e) {
                throw this.wrapInClientException(e);
            }
        }
        dataKeyCreationContext.close();
        return rawBsonDocument;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    BsonBinary encryptExplicitly(BsonValue value, EncryptOptions options) {
        BsonBinary bsonBinary;
        Assertions.notNull("value", value);
        Assertions.notNull("options", options);
        MongoExplicitEncryptOptions.Builder encryptOptionsBuilder = MongoExplicitEncryptOptions.builder().algorithm(options.getAlgorithm());
        if (options.getKeyId() != null) {
            encryptOptionsBuilder.keyId(options.getKeyId());
        }
        if (options.getKeyAltName() != null) {
            encryptOptionsBuilder.keyAltName(options.getKeyAltName());
        }
        MongoCryptContext encryptionContext = this.mongoCrypt.createExplicitEncryptionContext(new BsonDocument("v", value), encryptOptionsBuilder.build());
        try {
            bsonBinary = this.executeStateMachine(encryptionContext, null).getBinary("v");
        }
        catch (Throwable throwable) {
            try {
                encryptionContext.close();
                throw throwable;
            }
            catch (MongoCryptException e) {
                throw this.wrapInClientException(e);
            }
        }
        encryptionContext.close();
        return bsonBinary;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    BsonValue decryptExplicitly(BsonBinary value) {
        BsonValue bsonValue;
        Assertions.notNull("value", value);
        MongoCryptContext decryptionContext = this.mongoCrypt.createExplicitDecryptionContext(new BsonDocument("v", value));
        try {
            bsonValue = this.executeStateMachine(decryptionContext, null).get("v");
        }
        catch (Throwable throwable) {
            try {
                decryptionContext.close();
                throw throwable;
            }
            catch (MongoCryptException e) {
                throw this.wrapInClientException(e);
            }
        }
        decryptionContext.close();
        return bsonValue;
    }

    @Override
    public void close() {
        this.mongoCrypt.close();
        this.commandMarker.close();
        this.keyRetriever.close();
    }

    private RawBsonDocument executeStateMachine(MongoCryptContext cryptContext, String databaseName) {
        MongoCryptContext.State state;
        block7 : do {
            state = cryptContext.getState();
            switch (state) {
                case NEED_MONGO_COLLINFO: {
                    this.collInfo(cryptContext, databaseName);
                    continue block7;
                }
                case NEED_MONGO_MARKINGS: {
                    this.mark(cryptContext, databaseName);
                    continue block7;
                }
                case NEED_MONGO_KEYS: {
                    this.fetchKeys(cryptContext);
                    continue block7;
                }
                case NEED_KMS: {
                    this.decryptKeys(cryptContext);
                    continue block7;
                }
                case READY: {
                    return cryptContext.finish();
                }
            }
            break;
        } while (true);
        throw new MongoInternalException("Unsupported encryptor state + " + (Object)state);
    }

    private void collInfo(MongoCryptContext cryptContext, String databaseName) {
        try {
            BsonDocument collectionInfo = this.collectionInfoRetriever.filter(databaseName, cryptContext.getMongoOperation());
            if (collectionInfo != null) {
                cryptContext.addMongoOperationResult(collectionInfo);
            }
            cryptContext.completeMongoOperation();
        }
        catch (Throwable t) {
            throw MongoException.fromThrowableNonNull(t);
        }
    }

    private void mark(MongoCryptContext cryptContext, String databaseName) {
        try {
            RawBsonDocument markedCommand = this.commandMarker.mark(databaseName, cryptContext.getMongoOperation());
            cryptContext.addMongoOperationResult((BsonDocument)markedCommand);
            cryptContext.completeMongoOperation();
        }
        catch (Throwable t) {
            throw MongoException.fromThrowableNonNull(t);
        }
    }

    private void fetchKeys(MongoCryptContext keyBroker) {
        try {
            for (BsonDocument bsonDocument : this.keyRetriever.find(keyBroker.getMongoOperation())) {
                keyBroker.addMongoOperationResult(bsonDocument);
            }
            keyBroker.completeMongoOperation();
        }
        catch (Throwable t) {
            throw MongoException.fromThrowableNonNull(t);
        }
    }

    private void decryptKeys(MongoCryptContext cryptContext) {
        try {
            MongoKeyDecryptor keyDecryptor = cryptContext.nextKeyDecryptor();
            while (keyDecryptor != null) {
                this.decryptKey(keyDecryptor);
                keyDecryptor = cryptContext.nextKeyDecryptor();
            }
            cryptContext.completeKeyDecryptors();
        }
        catch (Throwable t) {
            throw MongoException.fromThrowableNonNull(t);
        }
    }

    private void decryptKey(MongoKeyDecryptor keyDecryptor) {
        InputStream inputStream = this.keyManagementService.stream(keyDecryptor.getHostName(), keyDecryptor.getMessage());
        try {
            int bytesNeeded = keyDecryptor.bytesNeeded();
            while (bytesNeeded > 0) {
                byte[] bytes = new byte[bytesNeeded];
                int bytesRead = inputStream.read(bytes, 0, bytes.length);
                keyDecryptor.feed(ByteBuffer.wrap(bytes, 0, bytesRead));
                bytesNeeded = keyDecryptor.bytesNeeded();
            }
        }
        catch (IOException e) {
            throw new MongoSocketReadException("Exception receiving message from key management service", new ServerAddress(keyDecryptor.getHostName(), this.keyManagementService.getPort()), e);
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException iOException) {}
        }
    }

    private MongoClientException wrapInClientException(MongoCryptException e) {
        return new MongoClientException("Exception in encryption library: " + e.getMessage(), e);
    }

}

