/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import com.mongodb.event.ServerMonitorListener;

public abstract class ServerMonitorListenerAdapter
implements ServerMonitorListener {
    @Override
    public void serverHearbeatStarted(ServerHeartbeatStartedEvent event) {
    }

    @Override
    public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent event) {
    }

    @Override
    public void serverHeartbeatFailed(ServerHeartbeatFailedEvent event) {
    }
}

