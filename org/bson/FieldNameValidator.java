/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

public interface FieldNameValidator {
    public boolean validate(String var1);

    public FieldNameValidator getValidatorForField(String var1);
}

