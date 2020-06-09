/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.assertions.Assertions;
import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongodb.lang.Nullable;
import java.util.Collections;
import java.util.List;

public final class MultiPolygon
extends Geometry {
    private final List<PolygonCoordinates> coordinates;

    public MultiPolygon(List<PolygonCoordinates> coordinates) {
        this(null, coordinates);
    }

    public MultiPolygon(@Nullable CoordinateReferenceSystem coordinateReferenceSystem, List<PolygonCoordinates> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        Assertions.isTrueArgument("coordinates has no null elements", !coordinates.contains(null));
        this.coordinates = Collections.unmodifiableList(coordinates);
    }

    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_POLYGON;
    }

    public List<PolygonCoordinates> getCoordinates() {
        return this.coordinates;
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
        MultiPolygon that = (MultiPolygon)o;
        return this.coordinates.equals(that.coordinates);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        return result;
    }

    public String toString() {
        return "MultiPolygon{coordinates=" + this.coordinates + (this.getCoordinateReferenceSystem() == null ? "" : ", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem()) + '}';
    }
}

