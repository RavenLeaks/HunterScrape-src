/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.METHOD, ElementType.CONSTRUCTOR})
public @interface BsonCreator {
}

