/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

public final class ClientSessionClock {
    public static final ClientSessionClock INSTANCE = new ClientSessionClock(0L);
    private long currentTime;

    private ClientSessionClock(long millis) {
        this.currentTime = millis;
    }

    public long now() {
        if (this.currentTime == 0L) {
            return System.currentTimeMillis();
        }
        return this.currentTime;
    }

    public void setTime(long millis) {
        this.currentTime = millis;
    }
}

