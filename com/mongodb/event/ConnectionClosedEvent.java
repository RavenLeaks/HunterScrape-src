/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.connection.ConnectionId;
import org.bson.assertions.Assertions;

@Deprecated
public final class ConnectionClosedEvent {
    private final ConnectionId connectionId;

    public ConnectionClosedEvent(ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public String toString() {
        return "ConnectionClosedEvent{connectionId=" + this.connectionId + '}';
    }
}

