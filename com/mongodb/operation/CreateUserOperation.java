/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.WriteConcern;
import com.mongodb.assertions.Assertions;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.binding.AsyncWriteBinding;
import com.mongodb.binding.WriteBinding;
import com.mongodb.connection.AsyncConnection;
import com.mongodb.connection.Connection;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.diagnostics.logging.Logger;
import com.mongodb.internal.async.ErrorHandlingResultCallback;
import com.mongodb.internal.operation.WriteConcernHelper;
import com.mongodb.operation.AsyncWriteOperation;
import com.mongodb.operation.CommandOperationHelper;
import com.mongodb.operation.OperationHelper;
import com.mongodb.operation.UserOperationHelper;
import com.mongodb.operation.WriteOperation;
import org.bson.BsonDocument;

@Deprecated
public class CreateUserOperation
implements AsyncWriteOperation<Void>,
WriteOperation<Void> {
    private final MongoCredential credential;
    private final boolean readOnly;
    private final WriteConcern writeConcern;

    public CreateUserOperation(MongoCredential credential, boolean readOnly) {
        this(credential, readOnly, null);
    }

    public CreateUserOperation(MongoCredential credential, boolean readOnly, WriteConcern writeConcern) {
        this.credential = Assertions.notNull("credential", credential);
        this.readOnly = readOnly;
        this.writeConcern = writeConcern;
    }

    public MongoCredential getCredential() {
        return this.credential;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, new OperationHelper.CallableWithConnection<Void>(){

            @Override
            public Void call(Connection connection) {
                try {
                    CommandOperationHelper.executeCommand(binding, CreateUserOperation.this.getCredential().getSource(), CreateUserOperation.this.getCommand(connection.getDescription()), connection, CommandOperationHelper.writeConcernErrorTransformer());
                }
                catch (MongoCommandException e) {
                    UserOperationHelper.translateUserCommandException(e);
                }
                return null;
            }
        });
    }

    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<Void> callback) {
        OperationHelper.withAsyncConnection(binding, new OperationHelper.AsyncCallableWithConnection(){

            @Override
            public void call(AsyncConnection connection, Throwable t) {
                SingleResultCallback<Object> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                } else {
                    SingleResultCallback<Void> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    CommandOperationHelper.executeCommandAsync(binding, CreateUserOperation.this.credential.getSource(), CreateUserOperation.this.getCommand(connection.getDescription()), connection, CommandOperationHelper.writeConcernErrorWriteTransformer(), UserOperationHelper.userCommandCallback(wrappedCallback));
                }
            }
        });
    }

    private BsonDocument getCommand(ConnectionDescription description) {
        BsonDocument commandDocument = UserOperationHelper.asCommandDocument(this.credential, description, this.readOnly, "createUser");
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }

}

