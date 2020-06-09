/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.ServerAddress;
import java.io.Serializable;

public final class ServerCursor
implements Serializable {
    private static final long serialVersionUID = -7013636754565190109L;
    private final long id;
    private final ServerAddress address;

    public ServerCursor(long id, ServerAddress address) {
        if (id == 0L) {
            throw new IllegalArgumentException();
        }
        this.id = id;
        this.address = address;
    }

    public long getId() {
        return this.id;
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
        ServerCursor that = (ServerCursor)o;
        if (this.id != that.id) {
            return false;
        }
        return this.address.equals(that.address);
    }

    public int hashCode() {
        int result = (int)(this.id ^ this.id >>> 32);
        result = 31 * result + this.address.hashCode();
        return result;
    }

    public String toString() {
        return "ServerCursor{getId=" + this.id + ", address=" + this.address + '}';
    }
}

