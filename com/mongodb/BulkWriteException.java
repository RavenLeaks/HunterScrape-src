/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.BulkWriteError;
import com.mongodb.BulkWriteResult;
import com.mongodb.MongoServerException;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcernError;
import java.util.List;

public class BulkWriteException
extends MongoServerException {
    private static final long serialVersionUID = -1505950263354313025L;
    private final BulkWriteResult writeResult;
    private final List<BulkWriteError> writeErrors;
    private final ServerAddress serverAddress;
    private final WriteConcernError writeConcernError;

    BulkWriteException(BulkWriteResult writeResult, List<BulkWriteError> writeErrors, WriteConcernError writeConcernError, ServerAddress serverAddress) {
        super("Bulk write operation error on server " + serverAddress + ". " + (writeErrors.isEmpty() ? "" : "Write errors: " + writeErrors + ". ") + (writeConcernError == null ? "" : "Write concern error: " + writeConcernError + ". "), serverAddress);
        this.writeResult = writeResult;
        this.writeErrors = writeErrors;
        this.writeConcernError = writeConcernError;
        this.serverAddress = serverAddress;
    }

    public BulkWriteResult getWriteResult() {
        return this.writeResult;
    }

    public List<BulkWriteError> getWriteErrors() {
        return this.writeErrors;
    }

    public WriteConcernError getWriteConcernError() {
        return this.writeConcernError;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BulkWriteException that = (BulkWriteException)o;
        if (!this.writeErrors.equals(that.writeErrors)) {
            return false;
        }
        if (!this.serverAddress.equals(that.serverAddress)) {
            return false;
        }
        if (this.writeConcernError != null ? !this.writeConcernError.equals(that.writeConcernError) : that.writeConcernError != null) {
            return false;
        }
        return this.writeResult.equals(that.writeResult);
    }

    public int hashCode() {
        int result = this.writeResult.hashCode();
        result = 31 * result + this.writeErrors.hashCode();
        result = 31 * result + this.serverAddress.hashCode();
        result = 31 * result + (this.writeConcernError != null ? this.writeConcernError.hashCode() : 0);
        return result;
    }
}

