/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBRef;
import com.mongodb.DBRefCodec;
import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;

public class DBRefCodecProvider
implements CodecProvider {
    @Override
    public <T> Codec<T> get(Class<T> clazz, CodecRegistry registry) {
        if (clazz == DBRef.class) {
            return new DBRefCodec(registry);
        }
        return null;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    public int hashCode() {
        return 0;
    }
}

