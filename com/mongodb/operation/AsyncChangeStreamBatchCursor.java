/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.MongoChangeStreamException;
import com.mongodb.async.AsyncAggregateResponseBatchCursor;
import com.mongodb.async.AsyncBatchCursor;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.binding.AsyncConnectionSource;
import com.mongodb.binding.AsyncReadBinding;
import com.mongodb.connection.ServerDescription;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.internal.async.ErrorHandlingResultCallback;
import com.mongodb.operation.ChangeStreamBatchCursorHelper;
import com.mongodb.operation.ChangeStreamOperation;
import com.mongodb.operation.OperationHelper;
import java.util.ArrayList;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonTimestamp;
import org.bson.RawBsonDocument;
import org.bson.codecs.Decoder;

final class AsyncChangeStreamBatchCursor<T>
implements AsyncAggregateResponseBatchCursor<T> {
    private final AsyncReadBinding binding;
    private final ChangeStreamOperation<T> changeStreamOperation;
    private volatile BsonDocument resumeToken;
    private volatile AsyncAggregateResponseBatchCursor<RawBsonDocument> wrapped;

    AsyncChangeStreamBatchCursor(ChangeStreamOperation<T> changeStreamOperation, AsyncAggregateResponseBatchCursor<RawBsonDocument> wrapped, AsyncReadBinding binding, BsonDocument resumeToken) {
        this.changeStreamOperation = changeStreamOperation;
        this.wrapped = wrapped;
        this.binding = binding;
        binding.retain();
        this.resumeToken = resumeToken;
    }

    AsyncAggregateResponseBatchCursor<RawBsonDocument> getWrapped() {
        return this.wrapped;
    }

    @Override
    public void next(SingleResultCallback<List<T>> callback) {
        this.resumeableOperation(new AsyncBlock(){

            @Override
            public void apply(AsyncAggregateResponseBatchCursor<RawBsonDocument> cursor, SingleResultCallback<List<RawBsonDocument>> callback) {
                cursor.next(callback);
                AsyncChangeStreamBatchCursor.this.cachePostBatchResumeToken(cursor);
            }
        }, this.convertResultsCallback(callback));
    }

    @Override
    public void tryNext(SingleResultCallback<List<T>> callback) {
        this.resumeableOperation(new AsyncBlock(){

            @Override
            public void apply(AsyncAggregateResponseBatchCursor<RawBsonDocument> cursor, SingleResultCallback<List<RawBsonDocument>> callback) {
                cursor.tryNext(callback);
                AsyncChangeStreamBatchCursor.this.cachePostBatchResumeToken(cursor);
            }
        }, this.convertResultsCallback(callback));
    }

    @Override
    public void close() {
        this.wrapped.close();
        this.binding.release();
    }

    @Override
    public void setBatchSize(int batchSize) {
        this.wrapped.setBatchSize(batchSize);
    }

    @Override
    public int getBatchSize() {
        return this.wrapped.getBatchSize();
    }

    @Override
    public boolean isClosed() {
        return this.wrapped.isClosed();
    }

    @Override
    public BsonDocument getPostBatchResumeToken() {
        return this.wrapped.getPostBatchResumeToken();
    }

    @Override
    public BsonTimestamp getOperationTime() {
        return this.changeStreamOperation.getStartAtOperationTime();
    }

    @Override
    public boolean isFirstBatchEmpty() {
        return this.wrapped.isFirstBatchEmpty();
    }

    private void cachePostBatchResumeToken(AsyncAggregateResponseBatchCursor<RawBsonDocument> queryBatchCursor) {
        if (queryBatchCursor.getPostBatchResumeToken() != null) {
            this.resumeToken = queryBatchCursor.getPostBatchResumeToken();
        }
    }

    private SingleResultCallback<List<RawBsonDocument>> convertResultsCallback(final SingleResultCallback<List<T>> callback) {
        return ErrorHandlingResultCallback.errorHandlingCallback(new SingleResultCallback<List<RawBsonDocument>>(){

            @Override
            public void onResult(List<RawBsonDocument> rawDocuments, Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                } else if (rawDocuments != null) {
                    ArrayList results = new ArrayList();
                    for (RawBsonDocument rawDocument : rawDocuments) {
                        if (!rawDocument.containsKey("_id")) {
                            callback.onResult(null, new MongoChangeStreamException("Cannot provide resume functionality when the resume token is missing."));
                            return;
                        }
                        results.add(rawDocument.decode(AsyncChangeStreamBatchCursor.this.changeStreamOperation.getDecoder()));
                    }
                    AsyncChangeStreamBatchCursor.this.resumeToken = rawDocuments.get(rawDocuments.size() - 1).getDocument("_id");
                    callback.onResult(results, null);
                } else {
                    callback.onResult(null, null);
                }
            }
        }, OperationHelper.LOGGER);
    }

    private void resumeableOperation(final AsyncBlock asyncBlock, final SingleResultCallback<List<RawBsonDocument>> callback) {
        asyncBlock.apply(this.wrapped, new SingleResultCallback<List<RawBsonDocument>>(){

            @Override
            public void onResult(List<RawBsonDocument> result, Throwable t) {
                if (t == null) {
                    callback.onResult(result, null);
                } else if (ChangeStreamBatchCursorHelper.isRetryableError(t)) {
                    AsyncChangeStreamBatchCursor.this.wrapped.close();
                    AsyncChangeStreamBatchCursor.this.retryOperation(asyncBlock, callback);
                } else {
                    callback.onResult(null, t);
                }
            }
        });
    }

    private void retryOperation(final AsyncBlock asyncBlock, final SingleResultCallback<List<RawBsonDocument>> callback) {
        OperationHelper.withAsyncReadConnection(this.binding, new OperationHelper.AsyncCallableWithSource(){

            @Override
            public void call(AsyncConnectionSource source, Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                } else {
                    AsyncChangeStreamBatchCursor.this.changeStreamOperation.setChangeStreamOptionsForResume(AsyncChangeStreamBatchCursor.this.resumeToken, source.getServerDescription().getMaxWireVersion());
                    source.release();
                    AsyncChangeStreamBatchCursor.this.changeStreamOperation.executeAsync(AsyncChangeStreamBatchCursor.this.binding, new SingleResultCallback<AsyncBatchCursor<T>>(){

                        @Override
                        public void onResult(AsyncBatchCursor<T> result, Throwable t) {
                            if (t != null) {
                                callback.onResult(null, t);
                            } else {
                                AsyncChangeStreamBatchCursor.this.wrapped = ((AsyncChangeStreamBatchCursor)result).getWrapped();
                                AsyncChangeStreamBatchCursor.this.binding.release();
                                AsyncChangeStreamBatchCursor.this.resumeableOperation(asyncBlock, callback);
                            }
                        }
                    });
                }
            }

        });
    }

    private static interface AsyncBlock {
        public void apply(AsyncAggregateResponseBatchCursor<RawBsonDocument> var1, SingleResultCallback<List<RawBsonDocument>> var2);
    }

}

