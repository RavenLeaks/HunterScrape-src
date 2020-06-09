/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.async;

import com.mongodb.assertions.Assertions;
import com.mongodb.async.SingleResultCallback;
import com.mongodb.diagnostics.logging.Logger;

public class ErrorHandlingResultCallback<T>
implements SingleResultCallback<T> {
    private final SingleResultCallback<T> wrapped;
    private final Logger logger;

    public static <T> SingleResultCallback<T> errorHandlingCallback(SingleResultCallback<T> callback, Logger logger) {
        if (callback instanceof ErrorHandlingResultCallback) {
            return callback;
        }
        return new ErrorHandlingResultCallback<T>(callback, logger);
    }

    ErrorHandlingResultCallback(SingleResultCallback<T> wrapped, Logger logger) {
        this.wrapped = Assertions.notNull("wrapped", wrapped);
        this.logger = Assertions.notNull("logger", logger);
    }

    @Override
    public void onResult(T result, Throwable t) {
        try {
            this.wrapped.onResult(result, t);
        }
        catch (Throwable e) {
            this.logger.error("Callback onResult call produced an error", e);
        }
    }
}

