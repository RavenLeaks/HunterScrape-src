/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import org.bson.BsonValue;
import org.bson.codecs.Codec;

public interface CollectibleCodec<T>
extends Codec<T> {
    public T generateIdIfAbsentFromDocument(T var1);

    public boolean documentHasId(T var1);

    public BsonValue getDocumentId(T var1);
}

