/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

import org.bson.FieldNameValidator;

class NoOpFieldNameValidator
implements FieldNameValidator {
    NoOpFieldNameValidator() {
    }

    @Override
    public boolean validate(String fieldName) {
        return true;
    }

    @Override
    public FieldNameValidator getValidatorForField(String fieldName) {
        return this;
    }
}

