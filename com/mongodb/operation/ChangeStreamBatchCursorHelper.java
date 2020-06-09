/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import com.mongodb.MongoChangeStreamException;
import com.mongodb.MongoCursorNotFoundException;
import com.mongodb.MongoException;
import com.mongodb.MongoInterruptedException;
import com.mongodb.MongoNotPrimaryException;
import com.mongodb.MongoSocketException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

final class ChangeStreamBatchCursorHelper {
    private static final List<Integer> UNRETRYABLE_SERVER_ERROR_CODES = Arrays.asList(136, 237, 280, 11601);
    private static final List<String> NONRESUMABLE_CHANGE_STREAM_ERROR_LABELS = Arrays.asList("NonResumableChangeStreamError");

    static boolean isRetryableError(Throwable t) {
        if (!(t instanceof MongoException) || t instanceof MongoChangeStreamException || t instanceof MongoInterruptedException) {
            return false;
        }
        if (t instanceof MongoNotPrimaryException || t instanceof MongoCursorNotFoundException || t instanceof MongoSocketException) {
            return true;
        }
        return !UNRETRYABLE_SERVER_ERROR_CODES.contains(((MongoException)t).getCode()) && Collections.disjoint(NONRESUMABLE_CHANGE_STREAM_ERROR_LABELS, ((MongoException)t).getErrorLabels());
    }

    private ChangeStreamBatchCursorHelper() {
    }
}

