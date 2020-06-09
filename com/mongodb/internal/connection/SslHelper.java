/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.SniSslHelper;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.net.ssl.SSLParameters;

public final class SslHelper {
    private static final SniSslHelper SNI_SSL_HELPER;

    public static void enableHostNameVerification(SSLParameters sslParameters) {
        sslParameters.setEndpointIdentificationAlgorithm("HTTPS");
    }

    public static void enableSni(String host, SSLParameters sslParameters) {
        if (SNI_SSL_HELPER != null) {
            SNI_SSL_HELPER.enableSni(host, sslParameters);
        }
    }

    private SslHelper() {
    }

    static {
        SniSslHelper sniSslHelper;
        try {
            sniSslHelper = (SniSslHelper)Class.forName("com.mongodb.internal.connection.Java8SniSslHelper").getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(e);
        }
        catch (InstantiationException e) {
            throw new ExceptionInInitializerError(e);
        }
        catch (IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
        catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
        catch (InvocationTargetException e) {
            throw new ExceptionInInitializerError(e.getTargetException());
        }
        catch (LinkageError t) {
            sniSslHelper = null;
        }
        SNI_SSL_HELPER = sniSslHelper;
    }
}

