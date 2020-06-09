/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.configuration;

import org.bson.codecs.Codec;

public interface CodecRegistry {
    public <T> Codec<T> get(Class<T> var1);
}

