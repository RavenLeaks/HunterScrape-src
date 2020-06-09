/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

import com.mongodb.client.model.Collation;
import com.mongodb.lang.Nullable;

public class DeleteOptions {
    private Collation collation;

    @Nullable
    public Collation getCollation() {
        return this.collation;
    }

    public DeleteOptions collation(@Nullable Collation collation) {
        this.collation = collation;
        return this;
    }

    public String toString() {
        return "DeleteOptions{collation=" + this.collation + '}';
    }
}

