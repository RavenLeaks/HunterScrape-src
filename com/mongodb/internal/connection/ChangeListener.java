/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.internal.connection.ChangeEvent;

interface ChangeListener<T> {
    public void stateChanged(ChangeEvent<T> var1);
}

