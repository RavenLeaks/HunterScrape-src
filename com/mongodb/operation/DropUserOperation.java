/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.MongoCommandException;
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
import org.bson.BsonString;
import org.bson.BsonValue;

@Deprecated
public class DropUserOperation
implements AsyncWriteOperation<Void>,
WriteOperation<Void> {
    private final String databaseName;
    private final String userName;
    private final WriteConcern writeConcern;

    public DropUserOperation(String databaseName, String userName) {
        this(databaseName, userName, null);
    }

    public DropUserOperation(String databaseName, String userName, WriteConcern writeConcern) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.userName = Assertions.notNull("userName", userName);
        this.writeConcern = writeConcern;
    }

    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, new OperationHelper.CallableWithConnection<Void>(){

            @Override
            public Void call(Connection connection) {
                try {
                    CommandOperationHelper.executeCommand(binding, DropUserOperation.this.databaseName, DropUserOperation.this.getCommand(connection.getDescription()), connection, CommandOperationHelper.writeConcernErrorTransformer());
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
                    CommandOperationHelper.executeCommandAsync(binding, DropUserOperation.this.databaseName, DropUserOperation.this.getCommand(connection.getDescription()), connection, CommandOperationHelper.writeConcernErrorWriteTransformer(), UserOperationHelper.userCommandCallback(wrappedCallback));
                }
            }
        });
    }

    private BsonDocument getCommand(ConnectionDescription description) {
        BsonDocument commandDocument = new BsonDocument("dropUser", new BsonString(this.userName));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }

}

