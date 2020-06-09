/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoCredential;
import com.mongodb.MongoSecurityException;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.connection.ConnectionDescription;
import com.mongodb.internal.authentication.NativeAuthenticationHelper;
import com.mongodb.internal.connection.Authenticator;
import com.mongodb.internal.connection.CommandHelper;
import com.mongodb.internal.connection.InternalConnection;
import com.mongodb.internal.connection.MongoCredentialWithCache;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

class NativeAuthenticator
extends Authenticator {
    NativeAuthenticator(MongoCredentialWithCache credential) {
        super(credential);
    }

    @Override
    public void authenticate(InternalConnection connection, ConnectionDescription connectionDescription) {
        try {
            BsonDocument nonceResponse = CommandHelper.executeCommand(this.getMongoCredential().getSource(), NativeAuthenticationHelper.getNonceCommand(), connection);
            BsonDocument authCommand = NativeAuthenticationHelper.getAuthCommand(this.getUserNameNonNull(), this.getPasswordNonNull(), ((BsonString)nonceResponse.get("nonce")).getValue());
            CommandHelper.executeCommand(this.getMongoCredential().getSource(), authCommand, connection);
        }
        catch (MongoCommandException e) {
            throw new MongoSecurityException(this.getMongoCredential(), "Exception authenticating", e);
        }
    }

    @Override
    void authenticateAsync(final InternalConnection connection, ConnectionDescription connectionDescription, final SingleResultCallback<Void> callback) {
        CommandHelper.executeCommandAsync(this.getMongoCredential().getSource(), NativeAuthenticationHelper.getNonceCommand(), connection, new SingleResultCallback<BsonDocument>(){

            @Override
            public void onResult(BsonDocument nonceResult, Throwable t) {
                if (t != null) {
                    callback.onResult(null, NativeAuthenticator.this.translateThrowable(t));
                } else {
                    CommandHelper.executeCommandAsync(NativeAuthenticator.this.getMongoCredential().getSource(), NativeAuthenticationHelper.getAuthCommand(NativeAuthenticator.this.getUserNameNonNull(), NativeAuthenticator.this.getPasswordNonNull(), ((BsonString)nonceResult.get("nonce")).getValue()), connection, new SingleResultCallback<BsonDocument>(){

                        @Override
                        public void onResult(BsonDocument result, Throwable t) {
                            if (t != null) {
                                callback.onResult(null, NativeAuthenticator.this.translateThrowable(t));
                            } else {
                                callback.onResult(null, null);
                            }
                        }
                    });
                }
            }

        });
    }

    private Throwable translateThrowable(Throwable t) {
        if (t instanceof MongoCommandException) {
            return new MongoSecurityException(this.getMongoCredential(), "Exception authenticating", t);
        }
        return t;
    }

}

