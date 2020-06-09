/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.MongoNamespace;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.assertions.Assertions;
import com.mongodb.async.AsyncAggregateResponseBatchCursor;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.binding.AsyncConnectionSource;
import com.mongodb.connection.AsyncConnection;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.connection.QueryResult;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.internal.async.ErrorHandlingResultCallback;
import com.mongodb.internal.operation.ServerVersionHelper;
import com.mongodb.internal.validator.NoOpFieldNameValidator;
import com.mongodb.operation.CommandResultDocumentCodec;
import com.mongodb.operation.CursorHelper;
import com.mongodb.operation.OperationHelper;
import com.mongodb.operation.QueryHelper;
import com.mongodb.session.SessionContext;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonTimestamp;
import org.bson.BsonValue;
import org.bson.FieldNameValidator;
import org.bson.codecs.BsonDocumentCodec;
import org.bson.codecs.Decoder;

class AsyncQueryBatchCursor<T>
implements AsyncAggregateResponseBatchCursor<T> {
    private static final FieldNameValidator NO_OP_FIELD_NAME_VALIDATOR = new NoOpFieldNameValidator();
    private static final String CURSOR = "cursor";
    private static final String POST_BATCH_RESUME_TOKEN = "postBatchResumeToken";
    private static final String OPERATION_TIME = "operationTime";
    private final MongoNamespace namespace;
    private final int limit;
    private final Decoder<T> decoder;
    private final long maxTimeMS;
    private final AsyncConnectionSource connectionSource;
    private final AtomicBoolean isClosed = new AtomicBoolean();
    private final AtomicReference<ServerCursor> cursor;
    private volatile QueryResult<T> firstBatch;
    private volatile int batchSize;
    private final AtomicInteger count = new AtomicInteger();
    private volatile BsonDocument postBatchResumeToken;
    private volatile BsonTimestamp operationTime;
    private volatile boolean firstBatchEmpty;

    AsyncQueryBatchCursor(QueryResult<T> firstBatch, int limit, int batchSize, long maxTimeMS, Decoder<T> decoder, AsyncConnectionSource connectionSource, AsyncConnection connection) {
        this(firstBatch, limit, batchSize, maxTimeMS, decoder, connectionSource, connection, null);
    }

    AsyncQueryBatchCursor(QueryResult<T> firstBatch, int limit, int batchSize, long maxTimeMS, Decoder<T> decoder, AsyncConnectionSource connectionSource, AsyncConnection connection, BsonDocument result) {
        Assertions.isTrueArgument("maxTimeMS >= 0", maxTimeMS >= 0L);
        this.maxTimeMS = maxTimeMS;
        this.namespace = firstBatch.getNamespace();
        this.firstBatch = firstBatch;
        this.limit = limit;
        this.batchSize = batchSize;
        this.decoder = decoder;
        this.cursor = new AtomicReference<ServerCursor>(firstBatch.getCursor());
        this.connectionSource = Assertions.notNull("connectionSource", connectionSource);
        this.count.addAndGet(firstBatch.getResults().size());
        if (result != null) {
            this.operationTime = result.getTimestamp(OPERATION_TIME, null);
            this.postBatchResumeToken = this.getPostBatchResumeTokenFromResponse(result);
        }
        this.firstBatchEmpty = firstBatch.getResults().isEmpty();
        if (firstBatch.getCursor() != null) {
            connectionSource.retain();
            if (this.limitReached()) {
                this.killCursor(connection);
            }
        }
    }

    @Override
    public void close() {
        if (!this.isClosed.getAndSet(true)) {
            this.killCursorOnClose();
        }
    }

    @Override
    public void next(SingleResultCallback<List<T>> callback) {
        this.next(callback, false);
    }

    @Override
    public void tryNext(SingleResultCallback<List<T>> callback) {
        this.next(callback, true);
    }

    @Override
    public void setBatchSize(int batchSize) {
        Assertions.isTrue("open", !this.isClosed.get());
        this.batchSize = batchSize;
    }

    @Override
    public int getBatchSize() {
        Assertions.isTrue("open", !this.isClosed.get());
        return this.batchSize;
    }

    @Override
    public boolean isClosed() {
        return this.isClosed.get();
    }

    @Override
    public BsonDocument getPostBatchResumeToken() {
        return this.postBatchResumeToken;
    }

    @Override
    public BsonTimestamp getOperationTime() {
        return this.operationTime;
    }

    @Override
    public boolean isFirstBatchEmpty() {
        return this.firstBatchEmpty;
    }

    private void next(SingleResultCallback<List<T>> callback, boolean tryNext) {
        if (this.isClosed()) {
            callback.onResult(null, new MongoException(String.format("%s called after the cursor was closed.", tryNext ? "tryNext()" : "next()")));
        } else if (this.firstBatch != null && (tryNext || !this.firstBatch.getResults().isEmpty())) {
            List<T> results = this.firstBatch.getResults();
            if (tryNext && results.isEmpty()) {
                results = null;
            }
            this.firstBatch = null;
            callback.onResult(results, null);
        } else {
            ServerCursor localCursor = this.getServerCursor();
            if (localCursor == null) {
                this.isClosed.set(true);
                callback.onResult(null, null);
            } else {
                this.getMore(localCursor, callback, tryNext);
            }
        }
    }

    private boolean limitReached() {
        return Math.abs(this.limit) != 0 && this.count.get() >= Math.abs(this.limit);
    }

    private void getMore(final ServerCursor cursor, final SingleResultCallback<List<T>> callback, final boolean tryNext) {
        this.connectionSource.getConnection(new SingleResultCallback<AsyncConnection>(){

            @Override
            public void onResult(AsyncConnection connection, Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                } else {
                    AsyncQueryBatchCursor.this.getMore(connection, cursor, callback, tryNext);
                }
            }
        });
    }

    private void getMore(AsyncConnection connection, ServerCursor cursor, SingleResultCallback<List<T>> callback, boolean tryNext) {
        if (ServerVersionHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
            connection.commandAsync(this.namespace.getDatabaseName(), this.asGetMoreCommandDocument(cursor.getId()), NO_OP_FIELD_NAME_VALIDATOR, ReadPreference.primary(), CommandResultDocumentCodec.create(this.decoder, "nextBatch"), this.connectionSource.getSessionContext(), new CommandResultSingleResultCallback(connection, cursor, callback, tryNext));
        } else {
            connection.getMoreAsync(this.namespace, cursor.getId(), CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count.get()), this.decoder, new QueryResultSingleResultCallback(connection, callback, tryNext));
        }
    }

    private BsonDocument asGetMoreCommandDocument(long cursorId) {
        BsonDocument document = new BsonDocument("getMore", new BsonInt64(cursorId)).append("collection", new BsonString(this.namespace.getCollectionName()));
        int batchSizeForGetMoreCommand = Math.abs(CursorHelper.getNumberToReturn(this.limit, this.batchSize, this.count.get()));
        if (batchSizeForGetMoreCommand != 0) {
            document.append("batchSize", new BsonInt32(batchSizeForGetMoreCommand));
        }
        if (this.maxTimeMS != 0L) {
            document.append("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        return document;
    }

    private void killCursorOnClose() {
        final ServerCursor localCursor = this.getServerCursor();
        if (localCursor != null) {
            this.connectionSource.getConnection(new SingleResultCallback<AsyncConnection>(){

                @Override
                public void onResult(AsyncConnection connection, Throwable t) {
                    if (t != null) {
                        AsyncQueryBatchCursor.this.connectionSource.release();
                    } else {
                        AsyncQueryBatchCursor.this.killCursorAsynchronouslyAndReleaseConnectionAndSource(connection, localCursor);
                    }
                }
            });
        }
    }

    private void killCursor(AsyncConnection connection) {
        ServerCursor localCursor = this.cursor.getAndSet(null);
        if (localCursor != null) {
            this.killCursorAsynchronouslyAndReleaseConnectionAndSource(connection.retain(), localCursor);
        } else {
            this.connectionSource.release();
        }
    }

    private void killCursorAsynchronouslyAndReleaseConnectionAndSource(final AsyncConnection connection, ServerCursor localCursor) {
        if (ServerVersionHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
            connection.commandAsync(this.namespace.getDatabaseName(), this.asKillCursorsCommandDocument(localCursor), NO_OP_FIELD_NAME_VALIDATOR, ReadPreference.primary(), new BsonDocumentCodec(), this.connectionSource.getSessionContext(), new SingleResultCallback<BsonDocument>(){

                @Override
                public void onResult(BsonDocument result, Throwable t) {
                    connection.release();
                    AsyncQueryBatchCursor.this.connectionSource.release();
                }
            });
        } else {
            connection.killCursorAsync(this.namespace, Collections.singletonList(localCursor.getId()), new SingleResultCallback<Void>(){

                @Override
                public void onResult(Void result, Throwable t) {
                    connection.release();
                    AsyncQueryBatchCursor.this.connectionSource.release();
                }
            });
        }
    }

    private BsonDocument asKillCursorsCommandDocument(ServerCursor localCursor) {
        return new BsonDocument("killCursors", new BsonString(this.namespace.getCollectionName())).append("cursors", new BsonArray(Collections.singletonList(new BsonInt64(localCursor.getId()))));
    }

    private void handleGetMoreQueryResult(AsyncConnection connection, SingleResultCallback<List<T>> callback, QueryResult<T> result, boolean tryNext) {
        if (this.isClosed()) {
            connection.release();
            callback.onResult(null, new MongoException(String.format("The cursor was closed before %s completed.", tryNext ? "tryNext()" : "next()")));
            return;
        }
        this.cursor.getAndSet(result.getCursor());
        if (!tryNext && result.getResults().isEmpty() && result.getCursor() != null) {
            this.getMore(connection, result.getCursor(), callback, false);
        } else {
            this.count.addAndGet(result.getResults().size());
            if (this.limitReached()) {
                this.killCursor(connection);
                connection.release();
            } else {
                connection.release();
                if (result.getCursor() == null) {
                    this.connectionSource.release();
                }
            }
            if (result.getResults().isEmpty()) {
                callback.onResult(null, null);
            } else {
                callback.onResult(result.getResults(), null);
            }
        }
    }

    ServerCursor getServerCursor() {
        return this.cursor.get();
    }

    private BsonDocument getPostBatchResumeTokenFromResponse(BsonDocument result) {
        BsonDocument cursor = result.getDocument(CURSOR, null);
        if (cursor != null) {
            return cursor.getDocument(POST_BATCH_RESUME_TOKEN, null);
        }
        return null;
    }

    private class QueryResultSingleResultCallback
    implements SingleResultCallback<QueryResult<T>> {
        private final AsyncConnection connection;
        private final SingleResultCallback<List<T>> callback;
        private final boolean tryNext;

        QueryResultSingleResultCallback(AsyncConnection connection, SingleResultCallback<List<T>> callback, boolean tryNext) {
            this.connection = connection;
            this.callback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
            this.tryNext = tryNext;
        }

        @Override
        public void onResult(QueryResult<T> result, Throwable t) {
            if (t != null) {
                this.connection.release();
                this.callback.onResult(null, t);
            } else {
                AsyncQueryBatchCursor.this.handleGetMoreQueryResult(this.connection, this.callback, result, this.tryNext);
            }
        }
    }

    private class CommandResultSingleResultCallback
    implements SingleResultCallback<BsonDocument> {
        private final AsyncConnection connection;
        private final ServerCursor cursor;
        private final SingleResultCallback<List<T>> callback;
        private final boolean tryNext;

        CommandResultSingleResultCallback(AsyncConnection connection, ServerCursor cursor, SingleResultCallback<List<T>> callback, boolean tryNext) {
            this.connection = connection;
            this.cursor = cursor;
            this.callback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
            this.tryNext = tryNext;
        }

        @Override
        public void onResult(BsonDocument result, Throwable t) {
            if (t != null) {
                Throwable translatedException = t instanceof MongoCommandException ? QueryHelper.translateCommandException((MongoCommandException)t, this.cursor) : t;
                this.connection.release();
                this.callback.onResult(null, translatedException);
            } else {
                QueryResult queryResult = OperationHelper.getMoreCursorDocumentToQueryResult(result.getDocument(AsyncQueryBatchCursor.CURSOR), this.connection.getDescription().getServerAddress());
                AsyncQueryBatchCursor.this.postBatchResumeToken = AsyncQueryBatchCursor.this.getPostBatchResumeTokenFromResponse(result);
                AsyncQueryBatchCursor.this.handleGetMoreQueryResult(this.connection, this.callback, queryResult, this.tryNext);
            }
        }
    }

}

