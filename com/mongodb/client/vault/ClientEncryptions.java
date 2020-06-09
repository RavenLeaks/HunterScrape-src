/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.vault;

import com.mongodb.ClientEncryptionSettings;
import com.mongodb.annotations.Beta;
import com.mongodb.client.internal.ClientEncryptionImpl;
import com.mongodb.client.vault.ClientEncryption;

@Beta
public final class ClientEncryptions {
    public static ClientEncryption create(ClientEncryptionSettings options) {
        return new ClientEncryptionImpl(options);
    }

    private ClientEncryptions() {
    }
}

