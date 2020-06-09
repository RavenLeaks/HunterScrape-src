/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.TransactionOptions;
import com.mongodb.binding.ConnectionSource;
import com.mongodb.binding.ReadBinding;
import com.mongodb.binding.ReadWriteBinding;
import com.mongodb.binding.ReferenceCounted;
import com.mongodb.binding.SingleServerBinding;
import com.mongodb.binding.WriteBinding;
import com.mongodb.client.ClientSession;
import com.mongodb.connection.Cluster;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ClusterType;
import com.mongodb.connection.Connection;
import com.mongodb.connection.Server;
import com.mongodb.connection.ServerDescription;
import com.mongodb.internal.binding.ClusterAwareReadWriteBinding;
import com.mongodb.internal.session.ClientSessionContext;
import com.mongodb.selector.ReadPreferenceServerSelector;
import com.mongodb.selector.ServerSelector;
import com.mongodb.session.SessionContext;
import org.bson.assertions.Assertions;

public class ClientSessionBinding
implements ReadWriteBinding {
    private final ClusterAwareReadWriteBinding wrapped;
    private final ClientSession session;
    private final boolean ownsSession;
    private final ClientSessionContext sessionContext;

    public ClientSessionBinding(ClientSession session, boolean ownsSession, ClusterAwareReadWriteBinding wrapped) {
        this.wrapped = wrapped;
        this.session = Assertions.notNull("session", session);
        this.ownsSession = ownsSession;
        this.sessionContext = new SyncClientSessionContext(session);
    }

    @Override
    public ReadPreference getReadPreference() {
        return this.wrapped.getReadPreference();
    }

    @Override
    public int getCount() {
        return this.wrapped.getCount();
    }

    @Override
    public ReadWriteBinding retain() {
        this.wrapped.retain();
        return this;
    }

    @Override
    public void release() {
        this.wrapped.release();
        this.closeSessionIfCountIsZero();
    }

    private void closeSessionIfCountIsZero() {
        if (this.getCount() == 0 && this.ownsSession) {
            this.session.close();
        }
    }

    @Override
    public ConnectionSource getReadConnectionSource() {
        return new SessionBindingConnectionSource(this.wrapConnectionSource(this.wrapped.getReadConnectionSource()));
    }

    @Override
    public ConnectionSource getWriteConnectionSource() {
        return new SessionBindingConnectionSource(this.wrapConnectionSource(this.wrapped.getWriteConnectionSource()));
    }

    private ConnectionSource wrapConnectionSource(ConnectionSource connectionSource) {
        ConnectionSource retVal = connectionSource;
        if (this.isActiveShardedTxn()) {
            this.setPinnedServerAddress();
            SingleServerBinding binding = new SingleServerBinding(this.wrapped.getCluster(), this.session.getPinnedServerAddress(), this.wrapped.getReadPreference());
            retVal = binding.getWriteConnectionSource();
            binding.release();
        }
        return retVal;
    }

    @Override
    public SessionContext getSessionContext() {
        return this.sessionContext;
    }

    private boolean isActiveShardedTxn() {
        return this.session.hasActiveTransaction() && this.wrapped.getCluster().getDescription().getType() == ClusterType.SHARDED;
    }

    private void setPinnedServerAddress() {
        if (this.session.getPinnedServerAddress() == null) {
            Server server = this.wrapped.getCluster().selectServer(new ReadPreferenceServerSelector(this.wrapped.getReadPreference()));
            this.session.setPinnedServerAddress(server.getDescription().getAddress());
        }
    }

    private final class SyncClientSessionContext
    extends ClientSessionContext
    implements SessionContext {
        private final ClientSession clientSession;

        SyncClientSessionContext(ClientSession clientSession) {
            super(clientSession);
            this.clientSession = clientSession;
        }

        @Override
        public boolean isImplicitSession() {
            return ClientSessionBinding.this.ownsSession;
        }

        @Override
        public boolean notifyMessageSent() {
            return this.clientSession.notifyMessageSent();
        }

        @Override
        public boolean hasActiveTransaction() {
            return this.clientSession.hasActiveTransaction();
        }

        @Override
        public ReadConcern getReadConcern() {
            if (this.clientSession.hasActiveTransaction()) {
                return this.clientSession.getTransactionOptions().getReadConcern();
            }
            return ClientSessionBinding.this.wrapped.getSessionContext().getReadConcern();
        }
    }

    private class SessionBindingConnectionSource
    implements ConnectionSource {
        private ConnectionSource wrapped;

        SessionBindingConnectionSource(ConnectionSource wrapped) {
            this.wrapped = wrapped;
        }

        @Override
        public ServerDescription getServerDescription() {
            return this.wrapped.getServerDescription();
        }

        @Override
        public SessionContext getSessionContext() {
            return ClientSessionBinding.this.sessionContext;
        }

        @Override
        public Connection getConnection() {
            return this.wrapped.getConnection();
        }

        @Override
        public ConnectionSource retain() {
            this.wrapped = this.wrapped.retain();
            return this;
        }

        @Override
        public int getCount() {
            return this.wrapped.getCount();
        }

        @Override
        public void release() {
            this.wrapped.release();
            ClientSessionBinding.this.closeSessionIfCountIsZero();
        }
    }

}

