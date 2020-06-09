/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.vault;

import com.mongodb.annotations.Beta;
import com.mongodb.client.model.vault.DataKeyOptions;
import com.mongodb.client.model.vault.EncryptOptions;
import java.io.Closeable;
import org.bson.BsonBinary;
import org.bson.BsonValue;

@Beta
public interface ClientEncryption
extends Closeable {
    public BsonBinary createDataKey(String var1);

    public BsonBinary createDataKey(String var1, DataKeyOptions var2);

    public BsonBinary encrypt(BsonValue var1, EncryptOptions var2);

    public BsonValue decrypt(BsonBinary var1);

    @Override
    public void close();
}

