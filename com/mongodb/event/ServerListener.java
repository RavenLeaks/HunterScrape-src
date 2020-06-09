/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.event;

import com.mongodb.event.ServerClosedEvent;
import com.mongodb.event.ServerDescriptionChangedEvent;
import com.mongodb.event.ServerOpeningEvent;
import java.util.EventListener;

public interface ServerListener
extends EventListener {
    public void serverOpening(ServerOpeningEvent var1);

    public void serverClosed(ServerClosedEvent var1);

    public void serverDescriptionChanged(ServerDescriptionChangedEvent var1);
}

