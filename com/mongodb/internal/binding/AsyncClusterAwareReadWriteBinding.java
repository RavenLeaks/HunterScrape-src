/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.binding;

import com.mongodb.binding.AsyncReadWriteBinding;
import com.mongodb.connection.Cluster;

public interface AsyncClusterAwareReadWriteBinding
extends AsyncReadWriteBinding {
    public Cluster getCluster();
}

