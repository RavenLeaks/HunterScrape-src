/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.annotations.Immutable;
import com.mongodb.client.model.geojson.CoordinateReferenceSystemType;

@Immutable
public abstract class CoordinateReferenceSystem {
    public abstract CoordinateReferenceSystemType getType();
}

