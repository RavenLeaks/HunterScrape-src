/*
 * Decompiled with CFR 0.145.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nonnull
 *  javax.annotation.meta.TypeQualifierNickname
 *  javax.annotation.meta.When
 */
package com.mongodb.lang;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.annotation.Nonnull;
import javax.annotation.meta.TypeQualifierNickname;
import javax.annotation.meta.When;

@Target(value={ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
@Retention(value=RetentionPolicy.RUNTIME)
@Documented
@Nonnull(when=When.MAYBE)
@TypeQualifierNickname
public @interface Nullable {
}

