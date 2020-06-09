/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ServerId;

public final class ConnectionPoolClosedEvent {
    private final ServerId serverId;

    public ConnectionPoolClosedEvent(ServerId serverId) {
        this.serverId = Assertions.notNull("serverId", serverId);
    }

    public ServerId getServerId() {
        return this.serverId;
    }

    public String toString() {
        return "ConnectionPoolClosedEvent{serverId=" + this.serverId + '}';
    }
}

