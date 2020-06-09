/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.gridfs.model;

@Deprecated
public final class GridFSDownloadByNameOptions {
    private int revision = -1;

    public GridFSDownloadByNameOptions revision(int revision) {
        this.revision = revision;
        return this;
    }

    public int getRevision() {
        return this.revision;
    }
}

