/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.ServerAddress;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.internal.connection.ReplyMessage;
import com.mongodb.internal.connection.ResponseBuffers;
import com.mongodb.internal.connection.ResponseCallback;
import java.util.List;
import org.bson.codecs.Decoder;

abstract class CommandResultBaseCallback<T>
extends ResponseCallback {
    public static final Logger LOGGER = Loggers.getLogger("protocol.command");
    private final Decoder<T> decoder;

    CommandResultBaseCallback(Decoder<T> decoder, long requestId, ServerAddress serverAddress) {
        super(requestId, serverAddress);
        this.decoder = decoder;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void callCallback(ResponseBuffers responseBuffers, Throwable t) {
        try {
            if (t != null || responseBuffers == null) {
                this.callCallback((T)null, t);
            } else {
                ReplyMessage<T> replyMessage = new ReplyMessage<T>(responseBuffers, this.decoder, this.getRequestId());
                this.callCallback(replyMessage.getDocuments().get(0), null);
            }
        }
        finally {
            try {
                if (responseBuffers != null) {
                    responseBuffers.close();
                }
            }
            catch (Throwable t1) {
                LOGGER.debug("GetMore ResponseBuffer close exception", t1);
            }
        }
    }

    protected abstract void callCallback(T var1, Throwable var2);
}

