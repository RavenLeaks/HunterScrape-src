/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ConnectionId;

@Deprecated
public final class ConnectionOpenedEvent {
    private final ConnectionId connectionId;

    public ConnectionOpenedEvent(ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public String toString() {
        return "ConnectionOpenedEvent{connectionId=" + this.connectionId + '}';
    }
}

