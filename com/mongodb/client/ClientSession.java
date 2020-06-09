/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client;

import com.mongodb.ServerAddress;
import com.mongodb.TransactionOptions;
import com.mongodb.client.TransactionBody;
import com.mongodb.lang.Nullable;

public interface ClientSession
extends com.mongodb.session.ClientSession {
    @Nullable
    @Override
    public ServerAddress getPinnedServerAddress();

    @Override
    public void setPinnedServerAddress(@Nullable ServerAddress var1);

    public boolean hasActiveTransaction();

    public boolean notifyMessageSent();

    public TransactionOptions getTransactionOptions();

    public void startTransaction();

    public void startTransaction(TransactionOptions var1);

    public void commitTransaction();

    public void abortTransaction();

    public <T> T withTransaction(TransactionBody<T> var1);

    public <T> T withTransaction(TransactionBody<T> var1, TransactionOptions var2);
}

