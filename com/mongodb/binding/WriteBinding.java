/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.binding;

import com.mongodb.binding.ConnectionSource;
import com.mongodb.binding.ReferenceCounted;
import com.mongodb.session.SessionContext;

@Deprecated
public interface WriteBinding
extends ReferenceCounted {
    public ConnectionSource getWriteConnectionSource();

    public SessionContext getSessionContext();

    @Override
    public WriteBinding retain();
}

