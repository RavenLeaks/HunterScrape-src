/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import org.bson.BsonReader;
import org.bson.codecs.DecoderContext;

public interface Decoder<T> {
    public T decode(BsonReader var1, DecoderContext var2);
}

