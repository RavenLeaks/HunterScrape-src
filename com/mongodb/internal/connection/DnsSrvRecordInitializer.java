/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.MongoException;
import com.mongodb.ServerAddress;
import com.mongodb.connection.ClusterType;
import java.util.Collection;

interface DnsSrvRecordInitializer {
    public void initialize(Collection<ServerAddress> var1);

    public void initialize(MongoException var1);

    public ClusterType getClusterType();
}

