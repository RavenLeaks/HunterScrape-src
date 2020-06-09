/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.client.model;

import com.mongodb.client.model.CountOptions;
import com.mongodb.client.model.EstimatedDocumentCountOptions;
import java.util.concurrent.TimeUnit;

public final class CountOptionsHelper {
    public static CountOptions fromEstimatedDocumentCountOptions(EstimatedDocumentCountOptions options) {
        return new CountOptions().maxTime(options.getMaxTime(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS);
    }

    private CountOptionsHelper() {
    }
}

