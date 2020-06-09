/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.SniSslHelper;
import java.util.Collections;
import java.util.List;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;

final class Java8SniSslHelper
implements SniSslHelper {
    @Override
    public void enableSni(String host, SSLParameters sslParameters) {
        try {
            SNIHostName sniHostName = new SNIHostName(host);
            sslParameters.setServerNames(Collections.singletonList(sniHostName));
        }
        catch (IllegalArgumentException sniHostName) {
            // empty catch block
        }
    }

    Java8SniSslHelper() {
    }

    static {
        try {
            Class.forName("javax.net.ssl.SNIHostName");
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}

