/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerListener;
import com.mongodb.event.ServerOpeningEvent;

public abstract class ServerListenerAdapter
implements ServerListener {
    @Override
    public void serverOpening(ServerOpeningEvent event) {
    }

    @Override
    public void serverClosed(ServerClosedEvent event) {
    }

    @Override
    public void serverDescriptionChanged(ServerDescriptionChangedEvent event) {
    }
}

