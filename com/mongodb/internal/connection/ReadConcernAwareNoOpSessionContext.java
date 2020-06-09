/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.ReadConcern;
import com.mongodb.assertions.Assertions;
import com.mongodb.internal.connection.NoOpSessionContext;

public final class ReadConcernAwareNoOpSessionContext
extends NoOpSessionContext {
    private final ReadConcern readConcern;

    public ReadConcernAwareNoOpSessionContext(ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
    }

    @Override
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
}

