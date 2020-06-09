/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.connection;

import com.mongodb.ServerAddress;
import com.mongodb.annotations.Immutable;
import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ClusterId;

@Immutable
public final class ServerId {
    private final ClusterId clusterId;
    private final ServerAddress address;

    public ServerId(ClusterId clusterId, ServerAddress address) {
        this.clusterId = Assertions.notNull("clusterId", clusterId);
        this.address = Assertions.notNull("address", address);
    }

    public ClusterId getClusterId() {
        return this.clusterId;
    }

    public ServerAddress getAddress() {
        return this.address;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ServerId serverId = (ServerId)o;
        if (!this.address.equals(serverId.address)) {
            return false;
        }
        return this.clusterId.equals(serverId.clusterId);
    }

    public int hashCode() {
        int result = this.clusterId.hashCode();
        result = 31 * result + this.address.hashCode();
        return result;
    }

    public String toString() {
        return "ServerId{clusterId=" + this.clusterId + ", address=" + this.address + '}';
    }
}

