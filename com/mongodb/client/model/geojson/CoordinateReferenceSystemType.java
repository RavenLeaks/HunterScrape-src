/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

public enum CoordinateReferenceSystemType {
    NAME("name"),
    LINK("link");
    
    private final String typeName;

    public String getTypeName() {
        return this.typeName;
    }

    private CoordinateReferenceSystemType(String typeName) {
        this.typeName = typeName;
    }
}

