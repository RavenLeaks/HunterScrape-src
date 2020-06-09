/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import javax.net.ssl.SSLParameters;

interface SniSslHelper {
    public void enableSni(String var1, SSLParameters var2);
}

