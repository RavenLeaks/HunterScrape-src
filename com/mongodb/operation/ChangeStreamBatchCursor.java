/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.Function;
import com.mongodb.MongoChangeStreamException;
import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.binding.ConnectionSource;
import com.mongodb.binding.ReadBinding;
import com.mongodb.connection.ServerDescription;
import com.mongodb.operation.AggregateResponseBatchCursor;
import com.mongodb.operation.ChangeStreamBatchCursorHelper;
import com.mongodb.operation.ChangeStreamOperation;
import com.mongodb.operation.OperationHelper;
import java.util.ArrayList;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.BsonTimestamp;
import org.bson.RawBsonDocument;
import org.bson.codecs.Decoder;

final class ChangeStreamBatchCursor<T>
implements AggregateResponseBatchCursor<T> {
    private final ReadBinding binding;
    private final ChangeStreamOperation<T> changeStreamOperation;
    private AggregateResponseBatchCursor<RawBsonDocument> wrapped;
    private BsonDocument resumeToken;

    ChangeStreamBatchCursor(ChangeStreamOperation<T> changeStreamOperation, AggregateResponseBatchCursor<RawBsonDocument> wrapped, ReadBinding binding, BsonDocument resumeToken) {
        this.changeStreamOperation = changeStreamOperation;
        this.binding = binding.retain();
        this.wrapped = wrapped;
        this.resumeToken = resumeToken;
    }

    AggregateResponseBatchCursor<RawBsonDocument> getWrapped() {
        return this.wrapped;
    }

    @Override
    public boolean hasNext() {
        return this.resumeableOperation(new Function<AggregateResponseBatchCursor<RawBsonDocument>, Boolean>(){

            @Override
            public Boolean apply(AggregateResponseBatchCursor<RawBsonDocument> queryBatchCursor) {
                return queryBatchCursor.hasNext();
            }
        });
    }

    @Override
    public List<T> next() {
        return (List)this.resumeableOperation(new Function<AggregateResponseBatchCursor<RawBsonDocument>, List<T>>(){

            @Override
            public List<T> apply(AggregateResponseBatchCursor<RawBsonDocument> queryBatchCursor) {
                List results = ChangeStreamBatchCursor.this.convertResults(queryBatchCursor.next());
                ChangeStreamBatchCursor.this.cachePostBatchResumeToken(queryBatchCursor);
                return results;
            }
        });
    }

    @Override
    public List<T> tryNext() {
        return (List)this.resumeableOperation(new Function<AggregateResponseBatchCursor<RawBsonDocument>, List<T>>(){

            @Override
            public List<T> apply(AggregateResponseBatchCursor<RawBsonDocument> queryBatchCursor) {
                List results = ChangeStreamBatchCursor.this.convertResults(queryBatchCursor.tryNext());
                ChangeStreamBatchCursor.this.cachePostBatchResumeToken(queryBatchCursor);
                return results;
            }
        });
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
    public ServerCursor getServerCursor() {
        return this.wrapped.getServerCursor();
    }

    @Override
    public ServerAddress getServerAddress() {
        return this.wrapped.getServerAddress();
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not implemented!");
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

    private void cachePostBatchResumeToken(AggregateResponseBatchCursor<RawBsonDocument> queryBatchCursor) {
        if (queryBatchCursor.getPostBatchResumeToken() != null) {
            this.resumeToken = queryBatchCursor.getPostBatchResumeToken();
        }
    }

    private List<T> convertResults(List<RawBsonDocument> rawDocuments) {
        ArrayList<T> results = null;
        if (rawDocuments != null) {
            results = new ArrayList<T>();
            for (RawBsonDocument rawDocument : rawDocuments) {
                if (!rawDocument.containsKey("_id")) {
                    throw new MongoChangeStreamException("Cannot provide resume functionality when the resume token is missing.");
                }
                results.add(rawDocument.decode(this.changeStreamOperation.getDecoder()));
            }
            this.resumeToken = rawDocuments.get(rawDocuments.size() - 1).getDocument("_id");
        }
        return results;
    }

    <R> R resumeableOperation(Function<AggregateResponseBatchCursor<RawBsonDocument>, R> function) {
        do {
            try {
                return function.apply(this.wrapped);
            }
            catch (Throwable t) {
                if (!ChangeStreamBatchCursorHelper.isRetryableError(t)) {
                    throw MongoException.fromThrowableNonNull(t);
                }
                this.wrapped.close();
                OperationHelper.withReadConnectionSource(this.binding, new OperationHelper.CallableWithSource<Void>(){

                    @Override
                    public Void call(ConnectionSource source) {
                        ChangeStreamBatchCursor.this.changeStreamOperation.setChangeStreamOptionsForResume(ChangeStreamBatchCursor.this.resumeToken, source.getServerDescription().getMaxWireVersion());
                        return null;
                    }
                });
                this.wrapped = ((ChangeStreamBatchCursor)this.changeStreamOperation.execute(this.binding)).getWrapped();
                this.binding.release();
                continue;
            }
            break;
        } while (true);
    }

}

