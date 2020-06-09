/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.binding;

@Deprecated
public interface ReferenceCounted {
    public int getCount();

    public ReferenceCounted retain();

    public void release();
}

