/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection.tlschannel;

import java.io.IOException;

public abstract class TlsChannelFlowControlException
extends IOException {
    private static final long serialVersionUID = 6237031103824382007L;

    @Override
    public Throwable fillInStackTrace() {
        return this;
    }
}

