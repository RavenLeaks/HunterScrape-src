/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.binding;

import com.mongodb.binding.ReadWriteBinding;
import com.mongodb.connection.Cluster;

public interface ClusterAwareReadWriteBinding
extends ReadWriteBinding {
    public Cluster getCluster();
}

