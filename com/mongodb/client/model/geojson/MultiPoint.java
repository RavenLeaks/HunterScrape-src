/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.assertions.Assertions;
import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.lang.Nullable;
import java.util.Collections;
import java.util.List;

public final class MultiPoint
extends Geometry {
    private final List<Position> coordinates;

    public MultiPoint(List<Position> coordinates) {
        this(null, coordinates);
    }

    public MultiPoint(@Nullable CoordinateReferenceSystem coordinateReferenceSystem, List<Position> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        Assertions.isTrueArgument("coordinates contains only non-null positions", !coordinates.contains(null));
        this.coordinates = Collections.unmodifiableList(coordinates);
    }

    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_POINT;
    }

    public List<Position> getCoordinates() {
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
        MultiPoint multiPoint = (MultiPoint)o;
        return this.coordinates.equals(multiPoint.coordinates);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        return 31 * result + this.coordinates.hashCode();
    }

    public String toString() {
        return "MultiPoint{coordinates=" + this.coordinates + (this.getCoordinateReferenceSystem() == null ? "" : ", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem()) + '}';
    }
}

