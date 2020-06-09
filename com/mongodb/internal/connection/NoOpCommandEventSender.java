/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.CommandEventSender;
import com.mongodb.internal.connection.ResponseBuffers;

class NoOpCommandEventSender
implements CommandEventSender {
    NoOpCommandEventSender() {
    }

    @Override
    public void sendStartedEvent() {
    }

    @Override
    public void sendFailedEvent(Throwable t) {
    }

    @Override
    public void sendSucceededEvent(ResponseBuffers responseBuffers) {
    }

    @Override
    public void sendSucceededEventForOneWayCommand() {
    }
}

