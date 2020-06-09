/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.connection.ServerId;

public final class ConnectionPoolWaitQueueExitedEvent {
    private final ServerId serverId;

    public ConnectionPoolWaitQueueExitedEvent(ServerId serverId) {
        this.serverId = serverId;
    }

    public ServerId getServerId() {
        return this.serverId;
    }

    public String toString() {
        return "ConnectionPoolWaitQueueExitedEvent{serverId=" + this.serverId + '}';
    }
}

