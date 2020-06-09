/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.assertions.Assertions;
import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.lang.Nullable;
import java.util.Collections;
import java.util.List;

public final class GeometryCollection
extends Geometry {
    private final List<? extends Geometry> geometries;

    public GeometryCollection(List<? extends Geometry> geometries) {
        this(null, geometries);
    }

    public GeometryCollection(@Nullable CoordinateReferenceSystem coordinateReferenceSystem, List<? extends Geometry> geometries) {
        super(coordinateReferenceSystem);
        Assertions.notNull("geometries", geometries);
        Assertions.isTrueArgument("geometries contains only non-null elements", !geometries.contains(null));
        this.geometries = Collections.unmodifiableList(geometries);
    }

    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.GEOMETRY_COLLECTION;
    }

    public List<? extends Geometry> getGeometries() {
        return this.geometries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        GeometryCollection that = (GeometryCollection)o;
        return this.geometries.equals(that.geometries);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.geometries.hashCode();
        return result;
    }

    public String toString() {
        CoordinateReferenceSystem coordinateReferenceSystem;
        return "GeometryCollection{geometries=" + this.geometries + ((coordinateReferenceSystem = this.getCoordinateReferenceSystem()) == null ? "" : ", coordinateReferenceSystem=" + coordinateReferenceSystem) + '}';
    }
}

