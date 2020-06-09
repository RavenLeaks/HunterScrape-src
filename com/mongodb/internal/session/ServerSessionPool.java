/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.session;

import com.mongodb.MongoException;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.assertions.Assertions;
import com.mongodb.connection.Cluster;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.Connection;
import com.mongodb.connection.Server;
import com.mongodb.connection.ServerDescription;
import com.mongodb.internal.connection.ConcurrentPool;
import com.mongodb.internal.connection.NoOpSessionContext;
import com.mongodb.internal.validator.NoOpFieldNameValidator;
import com.mongodb.selector.ReadPreferenceServerSelector;
import com.mongodb.selector.ServerSelector;
import com.mongodb.session.ServerSession;
import com.mongodb.session.SessionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.FieldNameValidator;
import org.bson.UuidRepresentation;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Decoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.UuidCodec;

public class ServerSessionPool {
    private static final int END_SESSIONS_BATCH_SIZE = 10000;
    private final ConcurrentPool<ServerSessionImpl> serverSessionPool = new ConcurrentPool<ServerSessionImpl>(Integer.MAX_VALUE, new ServerSessionItemFactory());
    private final Cluster cluster;
    private final Clock clock;
    private volatile boolean closing;
    private volatile boolean closed;
    private final List<BsonDocument> closedSessionIdentifiers = new ArrayList<BsonDocument>();

    public ServerSessionPool(Cluster cluster) {
        this(cluster, new Clock(){

            @Override
            public long millis() {
                return System.currentTimeMillis();
            }
        });
    }

    public ServerSessionPool(Cluster cluster, Clock clock) {
        this.cluster = cluster;
        this.clock = clock;
    }

    public ServerSession get() {
        Assertions.isTrue("server session pool is open", !this.closed);
        ServerSessionImpl serverSession = this.serverSessionPool.get();
        while (this.shouldPrune(serverSession)) {
            this.serverSessionPool.release(serverSession, true);
            serverSession = this.serverSessionPool.get();
        }
        return serverSession;
    }

    public void release(ServerSession serverSession) {
        this.serverSessionPool.release((ServerSessionImpl)serverSession);
        this.serverSessionPool.prune();
    }

    public void close() {
        try {
            this.closing = true;
            this.serverSessionPool.close();
            this.endClosedSessions();
        }
        finally {
            this.closed = true;
        }
    }

    public int getInUseCount() {
        return this.serverSessionPool.getInUseCount();
    }

    private void closeSession(ServerSessionImpl serverSession) {
        serverSession.close();
        if (!this.closing) {
            return;
        }
        this.closedSessionIdentifiers.add(serverSession.getIdentifier());
        if (this.closedSessionIdentifiers.size() == 10000) {
            this.endClosedSessions();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void endClosedSessions() {
        if (this.closedSessionIdentifiers.isEmpty()) {
            return;
        }
        final List<ServerDescription> primaryPreferred = new ReadPreferenceServerSelector(ReadPreference.primaryPreferred()).select(this.cluster.getCurrentDescription());
        if (primaryPreferred.isEmpty()) {
            return;
        }
        Connection connection = this.cluster.selectServer(new ServerSelector(){

            @Override
            public List<ServerDescription> select(ClusterDescription clusterDescription) {
                for (ServerDescription cur : clusterDescription.getServerDescriptions()) {
                    if (!cur.getAddress().equals(((ServerDescription)primaryPreferred.get(0)).getAddress())) continue;
                    return Collections.singletonList(cur);
                }
                return Collections.emptyList();
            }
        }).getConnection();
        try {
            connection.command("admin", new BsonDocument("endSessions", new BsonArray(this.closedSessionIdentifiers)), new NoOpFieldNameValidator(), ReadPreference.primaryPreferred(), new BsonDocumentCodec(), NoOpSessionContext.INSTANCE);
        }
        catch (MongoException mongoException) {
        }
        finally {
            this.closedSessionIdentifiers.clear();
            connection.release();
        }
    }

    private boolean shouldPrune(ServerSessionImpl serverSession) {
        long oneMinuteFromTimeout;
        Integer logicalSessionTimeoutMinutes = this.cluster.getCurrentDescription().getLogicalSessionTimeoutMinutes();
        if (logicalSessionTimeoutMinutes == null) {
            return false;
        }
        long currentTimeMillis = this.clock.millis();
        long timeSinceLastUse = currentTimeMillis - serverSession.getLastUsedAtMillis();
        return timeSinceLastUse > (oneMinuteFromTimeout = TimeUnit.MINUTES.toMillis(logicalSessionTimeoutMinutes - 1));
    }

    private final class ServerSessionItemFactory
    implements ConcurrentPool.ItemFactory<ServerSessionImpl> {
        private ServerSessionItemFactory() {
        }

        @Override
        public ServerSessionImpl create(boolean initialize) {
            return new ServerSessionImpl(this.createNewServerSessionIdentifier());
        }

        @Override
        public void close(ServerSessionImpl serverSession) {
            ServerSessionPool.this.closeSession(serverSession);
        }

        @Override
        public ConcurrentPool.Prune shouldPrune(ServerSessionImpl serverSession) {
            return ServerSessionPool.this.shouldPrune(serverSession) ? ConcurrentPool.Prune.YES : ConcurrentPool.Prune.STOP;
        }

        private BsonBinary createNewServerSessionIdentifier() {
            UuidCodec uuidCodec = new UuidCodec(UuidRepresentation.STANDARD);
            BsonDocument holder = new BsonDocument();
            BsonDocumentWriter bsonDocumentWriter = new BsonDocumentWriter(holder);
            bsonDocumentWriter.writeStartDocument();
            bsonDocumentWriter.writeName("id");
            uuidCodec.encode((BsonWriter)bsonDocumentWriter, UUID.randomUUID(), EncoderContext.builder().build());
            bsonDocumentWriter.writeEndDocument();
            return holder.getBinary("id");
        }
    }

    final class ServerSessionImpl
    implements ServerSession {
        private final BsonDocument identifier;
        private long transactionNumber = 0L;
        private volatile long lastUsedAtMillis = ServerSessionPool.access$100(ServerSessionPool.this).millis();
        private volatile boolean closed;

        ServerSessionImpl(BsonBinary identifier) {
            this.identifier = new BsonDocument("id", identifier);
        }

        void close() {
            this.closed = true;
        }

        long getLastUsedAtMillis() {
            return this.lastUsedAtMillis;
        }

        @Override
        public long getTransactionNumber() {
            return this.transactionNumber;
        }

        @Override
        public BsonDocument getIdentifier() {
            this.lastUsedAtMillis = ServerSessionPool.this.clock.millis();
            return this.identifier;
        }

        @Override
        public long advanceTransactionNumber() {
            ++this.transactionNumber;
            return this.transactionNumber;
        }

        @Override
        public boolean isClosed() {
            return this.closed;
        }
    }

    static interface Clock {
        public long millis();
    }

}

