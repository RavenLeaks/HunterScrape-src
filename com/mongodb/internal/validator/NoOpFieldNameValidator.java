/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.validator;

import org.bson.FieldNameValidator;

public class NoOpFieldNameValidator
implements FieldNameValidator {
    @Override
    public boolean validate(String fieldName) {
        return true;
    }

    @Override
    public FieldNameValidator getValidatorForField(String fieldName) {
        return this;
    }
}

