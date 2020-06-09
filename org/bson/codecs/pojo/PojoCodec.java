/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.codecs.Codec;
import org.bson.codecs.pojo.ClassModel;

abstract class PojoCodec<T>
implements Codec<T> {
    PojoCodec() {
    }

    abstract ClassModel<T> getClassModel();
}

