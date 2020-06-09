/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.selector;

import com.mongodb.assertions.Assertions;
import com.mongodb.connection.ClusterConnectionMode;
import com.mongodb.connection.ClusterDescription;
import com.mongodb.connection.ClusterSettings;
import com.mongodb.connection.ClusterType;
import com.mongodb.connection.ServerDescription;
import com.mongodb.connection.ServerSettings;
import com.mongodb.selector.ServerSelector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CompositeServerSelector
implements ServerSelector {
    private final List<ServerSelector> serverSelectors;

    public CompositeServerSelector(List<? extends ServerSelector> serverSelectors) {
        Assertions.notNull("serverSelectors", serverSelectors);
        if (serverSelectors.isEmpty()) {
            throw new IllegalArgumentException("Server selectors can not be an empty list");
        }
        ArrayList<ServerSelector> mergedServerSelectors = new ArrayList<ServerSelector>();
        for (ServerSelector cur : serverSelectors) {
            if (cur == null) {
                throw new IllegalArgumentException("Can not have a null server selector in the list of composed selectors");
            }
            if (cur instanceof CompositeServerSelector) {
                mergedServerSelectors.addAll(((CompositeServerSelector)cur).serverSelectors);
                continue;
            }
            mergedServerSelectors.add(cur);
        }
        this.serverSelectors = Collections.unmodifiableList(mergedServerSelectors);
    }

    public List<ServerSelector> getServerSelectors() {
        return this.serverSelectors;
    }

    @Override
    public List<ServerDescription> select(ClusterDescription clusterDescription) {
        ClusterDescription curClusterDescription = clusterDescription;
        List<ServerDescription> choices = null;
        for (ServerSelector cur : this.serverSelectors) {
            choices = cur.select(curClusterDescription);
            curClusterDescription = new ClusterDescription(clusterDescription.getConnectionMode(), clusterDescription.getType(), choices, clusterDescription.getClusterSettings(), clusterDescription.getServerSettings());
        }
        return choices;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        CompositeServerSelector that = (CompositeServerSelector)o;
        if (this.serverSelectors.size() != that.serverSelectors.size()) {
            return false;
        }
        return this.serverSelectors.equals(that.serverSelectors);
    }

    public int hashCode() {
        return this.serverSelectors != null ? this.serverSelectors.hashCode() : 0;
    }

    public String toString() {
        return "{serverSelectors=" + this.serverSelectors + '}';
    }
}

