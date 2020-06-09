/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

public interface IdGenerator<T> {
    public T generate();

    public Class<T> getType();
}

