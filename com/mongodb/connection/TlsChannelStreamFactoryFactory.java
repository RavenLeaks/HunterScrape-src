/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import com.mongodb.MongoClientException;
import com.mongodb.MongoSocketOpenException;
import com.mongodb.ServerAddress;
import com.mongodb.assertions.Assertions;
import com.mongodb.connection.AsyncCompletionHandler;
import com.mongodb.connection.BufferProvider;
import com.mongodb.connection.SocketSettings;
import com.mongodb.connection.SslSettings;
import com.mongodb.connection.Stream;
import com.mongodb.connection.StreamFactory;
import com.mongodb.connection.StreamFactoryFactory;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.diagnostics.logging.Loggers;
import com.mongodb.internal.connection.AsynchronousChannelStream;
import com.mongodb.internal.connection.ConcurrentLinkedDeque;
import com.mongodb.internal.connection.ExtendedAsynchronousByteChannel;
import com.mongodb.internal.connection.PowerOfTwoBufferPool;
import com.mongodb.internal.connection.SslHelper;
import com.mongodb.internal.connection.tlschannel.BufferAllocator;
import com.mongodb.internal.connection.tlschannel.ClientTlsChannel;
import com.mongodb.internal.connection.tlschannel.TlsChannel;
import com.mongodb.internal.connection.tlschannel.async.AsynchronousTlsChannel;
import com.mongodb.internal.connection.tlschannel.async.AsynchronousTlsChannelGroup;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.ByteChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.bson.ByteBuf;

public class TlsChannelStreamFactoryFactory
implements StreamFactoryFactory,
Closeable {
    private static final Logger LOGGER = Loggers.getLogger("connection.tls");
    private final SelectorMonitor selectorMonitor;
    private final AsynchronousTlsChannelGroup group;
    private final boolean ownsGroup;
    private final PowerOfTwoBufferPool bufferPool = new PowerOfTwoBufferPool();

    public TlsChannelStreamFactoryFactory() {
        this(new AsynchronousTlsChannelGroup(), true);
    }

    public TlsChannelStreamFactoryFactory(AsynchronousTlsChannelGroup group) {
        this(group, false);
    }

    private TlsChannelStreamFactoryFactory(AsynchronousTlsChannelGroup group, boolean ownsGroup) {
        this.group = group;
        this.ownsGroup = ownsGroup;
        this.selectorMonitor = new SelectorMonitor();
        this.selectorMonitor.start();
    }

    @Override
    public StreamFactory create(final SocketSettings socketSettings, final SslSettings sslSettings) {
        return new StreamFactory(){

            @Override
            public Stream create(ServerAddress serverAddress) {
                return new TlsChannelStream(serverAddress, socketSettings, sslSettings, TlsChannelStreamFactoryFactory.this.bufferPool, TlsChannelStreamFactoryFactory.this.group, TlsChannelStreamFactoryFactory.this.selectorMonitor);
            }
        };
    }

    @Override
    public void close() {
        this.selectorMonitor.close();
        if (this.ownsGroup) {
            this.group.shutdown();
        }
    }

    private static class TlsChannelStream
    extends AsynchronousChannelStream
    implements Stream {
        private final AsynchronousTlsChannelGroup group;
        private final SelectorMonitor selectorMonitor;
        private final SslSettings sslSettings;

        TlsChannelStream(ServerAddress serverAddress, SocketSettings settings, SslSettings sslSettings, BufferProvider bufferProvider, AsynchronousTlsChannelGroup group, SelectorMonitor selectorMonitor) {
            super(serverAddress, settings, bufferProvider);
            this.sslSettings = sslSettings;
            this.group = group;
            this.selectorMonitor = selectorMonitor;
        }

        @Override
        public void openAsync(final AsyncCompletionHandler<Void> handler) {
            Assertions.isTrue("unopened", this.getChannel() == null);
            try {
                final SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.setOption((SocketOption)StandardSocketOptions.TCP_NODELAY, (Object)true);
                socketChannel.setOption((SocketOption)StandardSocketOptions.SO_KEEPALIVE, (Object)true);
                if (this.getSettings().getReceiveBufferSize() > 0) {
                    socketChannel.setOption((SocketOption)StandardSocketOptions.SO_RCVBUF, (Object)this.getSettings().getReceiveBufferSize());
                }
                if (this.getSettings().getSendBufferSize() > 0) {
                    socketChannel.setOption((SocketOption)StandardSocketOptions.SO_SNDBUF, (Object)this.getSettings().getSendBufferSize());
                }
                socketChannel.connect(this.getServerAddress().getSocketAddress());
                this.selectorMonitor.register(socketChannel, new Runnable(){

                    @Override
                    public void run() {
                        try {
                            if (!socketChannel.finishConnect()) {
                                throw new MongoSocketOpenException("Failed to finish connect", TlsChannelStream.this.getServerAddress());
                            }
                            SSLEngine sslEngine = TlsChannelStream.this.getSslContext().createSSLEngine(TlsChannelStream.this.getServerAddress().getHost(), TlsChannelStream.this.getServerAddress().getPort());
                            sslEngine.setUseClientMode(true);
                            SSLParameters sslParameters = sslEngine.getSSLParameters();
                            SslHelper.enableSni(TlsChannelStream.this.getServerAddress().getHost(), sslParameters);
                            if (!TlsChannelStream.this.sslSettings.isInvalidHostNameAllowed()) {
                                SslHelper.enableHostNameVerification(sslParameters);
                            }
                            sslEngine.setSSLParameters(sslParameters);
                            BufferProviderAllocator bufferAllocator = new BufferProviderAllocator();
                            ClientTlsChannel tlsChannel = ((ClientTlsChannel.Builder)((ClientTlsChannel.Builder)ClientTlsChannel.newBuilder((ByteChannel)socketChannel, sslEngine).withEncryptedBufferAllocator(bufferAllocator)).withPlainBufferAllocator(bufferAllocator)).build();
                            TlsChannelStream.this.setChannel(new AsynchronousTlsChannel(TlsChannelStream.this.group, tlsChannel, socketChannel));
                            handler.completed(null);
                        }
                        catch (IOException e) {
                            handler.failed(new MongoSocketOpenException("Exception opening socket", TlsChannelStream.this.getServerAddress(), e));
                        }
                        catch (Throwable t) {
                            handler.failed(t);
                        }
                    }
                });
            }
            catch (IOException e) {
                handler.failed(new MongoSocketOpenException("Exception opening socket", this.getServerAddress(), e));
            }
            catch (Throwable t) {
                handler.failed(t);
            }
        }

        private SSLContext getSslContext() {
            try {
                return this.sslSettings.getContext() == null ? SSLContext.getDefault() : this.sslSettings.getContext();
            }
            catch (NoSuchAlgorithmException e) {
                throw new MongoClientException("Unable to create default SSLContext", e);
            }
        }

        private class BufferProviderAllocator
        implements BufferAllocator {
            private BufferProviderAllocator() {
            }

            @Override
            public ByteBuf allocate(int size) {
                return TlsChannelStream.this.getBufferProvider().getBuffer(size);
            }

            @Override
            public void free(ByteBuf buffer) {
                buffer.release();
            }
        }

    }

    private static class SelectorMonitor
    implements Closeable {
        private final Selector selector;
        private volatile boolean isClosed;
        private final ConcurrentLinkedDeque<Pair> pendingRegistrations = new ConcurrentLinkedDeque();

        SelectorMonitor() {
            try {
                this.selector = Selector.open();
            }
            catch (IOException e) {
                throw new MongoClientException("Exception opening Selector", e);
            }
        }

        void start() {
            Thread selectorThread = new Thread(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        while (!SelectorMonitor.this.isClosed) {
                            try {
                                SelectorMonitor.this.selector.select();
                                for (SelectionKey selectionKey : SelectorMonitor.this.selector.selectedKeys()) {
                                    selectionKey.cancel();
                                    Runnable runnable = (Runnable)selectionKey.attachment();
                                    runnable.run();
                                }
                                Iterator iter = SelectorMonitor.this.pendingRegistrations.iterator();
                                while (iter.hasNext()) {
                                    Pair pendingRegistration = (Pair)iter.next();
                                    pendingRegistration.socketChannel.register(SelectorMonitor.this.selector, 8, pendingRegistration.attachment);
                                    iter.remove();
                                }
                            }
                            catch (IOException e) {
                                LOGGER.warn("Exception in selector loop", e);
                            }
                            catch (RuntimeException e) {
                                LOGGER.warn("Exception in selector loop", e);
                            }
                        }
                    }
                    finally {
                        try {
                            SelectorMonitor.this.selector.close();
                        }
                        catch (IOException e) {}
                    }
                }
            });
            selectorThread.setDaemon(true);
            selectorThread.start();
        }

        void register(SocketChannel channel, Runnable attachment) {
            this.pendingRegistrations.add(new Pair(channel, attachment));
            this.selector.wakeup();
        }

        @Override
        public void close() {
            this.isClosed = true;
            this.selector.wakeup();
        }

        private static final class Pair {
            private final SocketChannel socketChannel;
            private final Runnable attachment;

            private Pair(SocketChannel socketChannel, Runnable attachment) {
                this.socketChannel = socketChannel;
                this.attachment = attachment;
            }
        }

    }

}

