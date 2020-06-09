/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ConnectionId;

public final class ConnectionCheckedInEvent {
    private final ConnectionId connectionId;

    public ConnectionCheckedInEvent(ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public String toString() {
        return "ConnectionCheckedInEvent{connectionId=" + this.connectionId + '}';
    }
}

