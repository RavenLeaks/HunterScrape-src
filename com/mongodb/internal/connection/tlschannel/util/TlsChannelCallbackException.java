/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection.tlschannel.util;

import javax.net.ssl.SSLException;

public class TlsChannelCallbackException
extends SSLException {
    private static final long serialVersionUID = -8618230576763080549L;

    public TlsChannelCallbackException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

