/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.event.ServerHeartbeatFailedEvent;
import com.mongodb.event.ServerHeartbeatStartedEvent;
import com.mongodb.event.ServerHeartbeatSucceededEvent;
import java.util.EventListener;

public interface ServerMonitorListener
extends EventListener {
    public void serverHearbeatStarted(ServerHeartbeatStartedEvent var1);

    public void serverHeartbeatSucceeded(ServerHeartbeatSucceededEvent var1);

    public void serverHeartbeatFailed(ServerHeartbeatFailedEvent var1);
}

