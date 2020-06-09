/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.codecs.pojo.InstanceCreator;

public interface InstanceCreatorFactory<T> {
    public InstanceCreator<T> create();
}

