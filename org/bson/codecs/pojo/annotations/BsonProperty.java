/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo.annotations;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(value={ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(value=RetentionPolicy.RUNTIME)
public @interface BsonProperty {
    public String value() default "";

    public boolean useDiscriminator() default false;
}

