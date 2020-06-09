/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.internal;

import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoClientException;
import com.mongodb.MongoException;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoInternalException;
import com.mongodb.ReadConcern;
import com.mongodb.ServerAddress;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import com.mongodb.assertions.Assertions;
import com.mongodb.client.ClientSession;
import com.mongodb.client.TransactionBody;
import com.mongodb.client.internal.ClientSessionClock;
import com.mongodb.client.internal.MongoClientDelegate;
import com.mongodb.client.internal.OperationExecutor;
import com.mongodb.internal.session.BaseClientSessionImpl;
import com.mongodb.internal.session.ServerSessionPool;
import com.mongodb.operation.AbortTransactionOperation;
import com.mongodb.operation.CommitTransactionOperation;
import com.mongodb.operation.WriteOperation;
import com.mongodb.session.ServerSession;
import java.util.concurrent.TimeUnit;
import org.bson.BsonDocument;

final class ClientSessionImpl
extends BaseClientSessionImpl
implements ClientSession {
    private static final int MAX_RETRY_TIME_LIMIT_MS = 120000;
    private final MongoClientDelegate delegate;
    private TransactionState transactionState = TransactionState.NONE;
    private boolean messageSentInCurrentTransaction;
    private boolean commitInProgress;
    private TransactionOptions transactionOptions;

    ClientSessionImpl(ServerSessionPool serverSessionPool, Object originator, ClientSessionOptions options, MongoClientDelegate delegate) {
        super(serverSessionPool, originator, options);
        this.delegate = delegate;
    }

    @Override
    public boolean hasActiveTransaction() {
        return this.transactionState == TransactionState.IN || this.transactionState == TransactionState.COMMITTED && this.commitInProgress;
    }

    @Override
    public boolean notifyMessageSent() {
        if (this.hasActiveTransaction()) {
            boolean firstMessageInCurrentTransaction = !this.messageSentInCurrentTransaction;
            this.messageSentInCurrentTransaction = true;
            return firstMessageInCurrentTransaction;
        }
        if (this.transactionState == TransactionState.COMMITTED || this.transactionState == TransactionState.ABORTED) {
            this.cleanupTransaction(TransactionState.NONE);
        }
        return false;
    }

    @Override
    public TransactionOptions getTransactionOptions() {
        Assertions.isTrue("in transaction", this.transactionState == TransactionState.IN || this.transactionState == TransactionState.COMMITTED);
        return this.transactionOptions;
    }

    @Override
    public void startTransaction() {
        this.startTransaction(TransactionOptions.builder().build());
    }

    @Override
    public void startTransaction(TransactionOptions transactionOptions) {
        Assertions.notNull("transactionOptions", transactionOptions);
        if (this.transactionState == TransactionState.IN) {
            throw new IllegalStateException("Transaction already in progress");
        }
        if (this.transactionState == TransactionState.COMMITTED) {
            this.cleanupTransaction(TransactionState.IN);
        } else {
            this.transactionState = TransactionState.IN;
        }
        this.getServerSession().advanceTransactionNumber();
        this.transactionOptions = TransactionOptions.merge(transactionOptions, this.getOptions().getDefaultTransactionOptions());
        WriteConcern writeConcern = this.transactionOptions.getWriteConcern();
        if (writeConcern == null) {
            throw new MongoInternalException("Invariant violated.  Transaction options write concern can not be null");
        }
        if (!writeConcern.isAcknowledged()) {
            throw new MongoClientException("Transactions do not support unacknowledged write concern");
        }
        this.setPinnedServerAddress(null);
    }

    @Override
    public void commitTransaction() {
        if (this.transactionState == TransactionState.ABORTED) {
            throw new IllegalStateException("Cannot call commitTransaction after calling abortTransaction");
        }
        if (this.transactionState == TransactionState.NONE) {
            throw new IllegalStateException("There is no transaction started");
        }
        try {
            if (this.messageSentInCurrentTransaction) {
                ReadConcern readConcern = this.transactionOptions.getReadConcern();
                if (readConcern == null) {
                    throw new MongoInternalException("Invariant violated.  Transaction options read concern can not be null");
                }
                this.commitInProgress = true;
                this.delegate.getOperationExecutor().execute(new CommitTransactionOperation(this.transactionOptions.getWriteConcern(), this.transactionState == TransactionState.COMMITTED).recoveryToken(this.getRecoveryToken()).maxCommitTime(this.transactionOptions.getMaxCommitTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS), readConcern, this);
            }
        }
        catch (MongoException e) {
            this.unpinServerAddressOnError(e);
            throw e;
        }
        finally {
            this.transactionState = TransactionState.COMMITTED;
            this.commitInProgress = false;
        }
    }

    @Override
    public void abortTransaction() {
        if (this.transactionState == TransactionState.ABORTED) {
            throw new IllegalStateException("Cannot call abortTransaction twice");
        }
        if (this.transactionState == TransactionState.COMMITTED) {
            throw new IllegalStateException("Cannot call abortTransaction after calling commitTransaction");
        }
        if (this.transactionState == TransactionState.NONE) {
            throw new IllegalStateException("There is no transaction started");
        }
        try {
            if (this.messageSentInCurrentTransaction) {
                ReadConcern readConcern = this.transactionOptions.getReadConcern();
                if (readConcern == null) {
                    throw new MongoInternalException("Invariant violated.  Transaction options read concern can not be null");
                }
                this.delegate.getOperationExecutor().execute(new AbortTransactionOperation(this.transactionOptions.getWriteConcern()).recoveryToken(this.getRecoveryToken()), readConcern, this);
            }
        }
        catch (Exception e) {
            if (e instanceof MongoException) {
                this.unpinServerAddressOnError((MongoException)e);
            }
        }
        finally {
            this.cleanupTransaction(TransactionState.ABORTED);
        }
    }

    private void unpinServerAddressOnError(MongoException e) {
        if (e.hasErrorLabel("TransientTransactionError") || e.hasErrorLabel("UnknownTransactionCommitResult")) {
            this.setPinnedServerAddress(null);
        }
    }

    @Override
    public <T> T withTransaction(TransactionBody<T> transactionBody) {
        return this.withTransaction(transactionBody, TransactionOptions.builder().build());
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    public <T> T withTransaction(TransactionBody<T> transactionBody, TransactionOptions options) {
        Assertions.notNull("transactionBody", transactionBody);
        long startTime = ClientSessionClock.INSTANCE.now();
        block4 : do {
            T retVal;
            try {
                this.startTransaction(options);
                retVal = transactionBody.execute();
            }
            catch (RuntimeException e) {
                if (this.transactionState == TransactionState.IN) {
                    this.abortTransaction();
                }
                if (!(e instanceof MongoException)) throw e;
                if (!((MongoException)e).hasErrorLabel("TransientTransactionError")) throw e;
                if (ClientSessionClock.INSTANCE.now() - startTime >= 120000L) throw e;
                continue;
            }
            if (this.transactionState != TransactionState.IN) return retVal;
            do {
                try {
                    this.commitTransaction();
                    return retVal;
                }
                catch (MongoException e) {
                    this.unpinServerAddressOnError(e);
                    if (ClientSessionClock.INSTANCE.now() - startTime >= 120000L) throw e;
                    this.applyMajorityWriteConcernToTransactionOptions();
                    if (!(e instanceof MongoExecutionTimeoutException) && e.hasErrorLabel("UnknownTransactionCommitResult")) continue;
                    if (!e.hasErrorLabel("TransientTransactionError")) throw e;
                    continue block4;
                }
                break;
            } while (true);
            break;
        } while (true);
    }

    private void applyMajorityWriteConcernToTransactionOptions() {
        WriteConcern writeConcern;
        this.transactionOptions = this.transactionOptions != null ? ((writeConcern = this.transactionOptions.getWriteConcern()) != null ? TransactionOptions.merge(TransactionOptions.builder().writeConcern(writeConcern.withW("majority")).build(), this.transactionOptions) : TransactionOptions.merge(TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build(), this.transactionOptions)) : TransactionOptions.builder().writeConcern(WriteConcern.MAJORITY).build();
    }

    @Override
    public void close() {
        try {
            if (this.transactionState == TransactionState.IN) {
                this.abortTransaction();
            }
        }
        finally {
            super.close();
        }
    }

    private void cleanupTransaction(TransactionState nextState) {
        this.messageSentInCurrentTransaction = false;
        this.transactionOptions = null;
        this.transactionState = nextState;
    }

    private static enum TransactionState {
        NONE,
        IN,
        COMMITTED,
        ABORTED;
        
    }

}

