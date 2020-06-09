/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.codecs.Codec;
import org.bson.codecs.pojo.TypeWithTypeParameters;

public interface PropertyCodecRegistry {
    public <T> Codec<T> get(TypeWithTypeParameters<T> var1);
}

