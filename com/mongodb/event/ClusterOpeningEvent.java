/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ClusterId;

public final class ClusterOpeningEvent {
    private final ClusterId clusterId;

    public ClusterOpeningEvent(ClusterId clusterId) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
    }

    public ClusterId getClusterId() {
        return this.clusterId;
    }

    public String toString() {
        return "ClusterOpeningEvent{clusterId=" + this.clusterId + '}';
    }
}

