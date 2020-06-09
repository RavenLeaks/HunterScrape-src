/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ConnectionId;

public final class ServerHeartbeatStartedEvent {
    private final ConnectionId connectionId;

    public ServerHeartbeatStartedEvent(ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }

    public ConnectionId getConnectionId() {
        return this.connectionId;
    }

    public String toString() {
        return "ServerHeartbeatStartedEvent{connectionId=" + this.connectionId + "} " + super.toString();
    }
}

