/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.codecs.pojo.PropertySerialization;

class PropertyModelSerializationImpl<T>
implements PropertySerialization<T> {
    PropertyModelSerializationImpl() {
    }

    @Override
    public boolean shouldSerialize(T value) {
        return value != null;
    }
}

