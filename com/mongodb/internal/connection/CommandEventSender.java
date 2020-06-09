/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.ResponseBuffers;

interface CommandEventSender {
    public void sendStartedEvent();

    public void sendFailedEvent(Throwable var1);

    public void sendSucceededEvent(ResponseBuffers var1);

    public void sendSucceededEventForOneWayCommand();
}

