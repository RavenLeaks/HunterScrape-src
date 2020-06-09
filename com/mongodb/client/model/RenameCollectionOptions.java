/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

public class RenameCollectionOptions {
    private boolean dropTarget;

    public boolean isDropTarget() {
        return this.dropTarget;
    }

    public RenameCollectionOptions dropTarget(boolean dropTarget) {
        this.dropTarget = dropTarget;
        return this;
    }

    public String toString() {
        return "RenameCollectionOptions{dropTarget=" + this.dropTarget + '}';
    }
}

