/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.selector;

import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ServerDescription;
import com.mongodb.selector.ServerSelector;
import java.util.List;

@Deprecated
public final class PrimaryServerSelector
implements ServerSelector {
    @Override
    public List<ServerDescription> select(ClusterDescription clusterDescription) {
        return clusterDescription.getPrimaries();
    }

    public String toString() {
        return "PrimaryServerSelector";
    }
}

