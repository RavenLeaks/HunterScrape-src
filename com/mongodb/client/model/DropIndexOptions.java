/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

import com.mongodb.assertions.Assertions;
import java.util.concurrent.TimeUnit;

public class DropIndexOptions {
    private long maxTimeMS;

    public long getMaxTime(TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }

    public DropIndexOptions maxTime(long maxTime, TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }

    public String toString() {
        return "DropIndexOptions{maxTimeMS=" + this.maxTimeMS + '}';
    }
}

