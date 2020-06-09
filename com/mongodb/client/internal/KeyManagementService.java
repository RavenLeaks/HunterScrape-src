/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.MongoSocketOpenException;
import com.mongodb.MongoSocketReadException;
import com.mongodb.MongoSocketWriteException;
import com.mongodb.ServerAddress;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

class KeyManagementService {
    private final SSLContext sslContext;
    private final int port;
    private final int timeoutMillis;

    KeyManagementService(SSLContext sslContext, int port, int timeoutMillis) {
        this.sslContext = sslContext;
        this.port = port;
        this.timeoutMillis = timeoutMillis;
    }

    public InputStream stream(String host, ByteBuffer message) {
        Socket socket;
        try {
            socket = this.sslContext.getSocketFactory().createSocket();
        }
        catch (IOException e) {
            throw new MongoSocketOpenException("Exception opening connection to Key Management Service", new ServerAddress(host, this.port), e);
        }
        try {
            socket.setSoTimeout(this.timeoutMillis);
            socket.connect(new InetSocketAddress(InetAddress.getByName(host), this.port), this.timeoutMillis);
        }
        catch (IOException e) {
            this.closeSocket(socket);
            throw new MongoSocketOpenException("Exception opening connection to Key Management Service", new ServerAddress(host, this.port), e);
        }
        try {
            OutputStream outputStream = socket.getOutputStream();
            byte[] bytes = new byte[message.remaining()];
            message.get(bytes);
            outputStream.write(bytes);
        }
        catch (IOException e) {
            this.closeSocket(socket);
            throw new MongoSocketWriteException("Exception sending message to Key Management Service", new ServerAddress(host, this.port), e);
        }
        try {
            return socket.getInputStream();
        }
        catch (IOException e) {
            this.closeSocket(socket);
            throw new MongoSocketReadException("Exception receiving message from Key Management Service", new ServerAddress(host, this.port), e);
        }
    }

    public int getPort() {
        return this.port;
    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

