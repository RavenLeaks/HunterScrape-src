/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.assertions.Assertions;
import com.mongodb.internal.connection.ReadConcernHelper;
import com.mongodb.session.SessionContext;
import org.bson.BsonDocument;
import org.bson.BsonValue;

final class OperationReadConcernHelper {
    static void appendReadConcernToCommand(SessionContext sessionContext, BsonDocument commandDocument) {
        Assertions.notNull("commandDocument", commandDocument);
        Assertions.notNull("sessionContext", sessionContext);
        if (sessionContext.hasActiveTransaction()) {
            return;
        }
        BsonDocument readConcernDocument = ReadConcernHelper.getReadConcernDocument(sessionContext);
        if (!readConcernDocument.isEmpty()) {
            commandDocument.append("readConcern", readConcernDocument);
        }
    }

    private OperationReadConcernHelper() {
    }
}

