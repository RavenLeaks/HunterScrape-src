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

public final class MultiLineString
extends Geometry {
    private final List<List<Position>> coordinates;

    public MultiLineString(List<List<Position>> coordinates) {
        this(null, coordinates);
    }

    public MultiLineString(@Nullable CoordinateReferenceSystem coordinateReferenceSystem, List<List<Position>> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        for (List<Position> line : coordinates) {
            Assertions.notNull("line", line);
            Assertions.isTrueArgument("line contains only non-null positions", !line.contains(null));
        }
        this.coordinates = Collections.unmodifiableList(coordinates);
    }

    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_LINE_STRING;
    }

    public List<List<Position>> getCoordinates() {
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
        MultiLineString polygon = (MultiLineString)o;
        return this.coordinates.equals(polygon.coordinates);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        return result;
    }

    public String toString() {
        return "MultiLineString{coordinates=" + this.coordinates + (this.getCoordinateReferenceSystem() == null ? "" : ", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem()) + '}';
    }
}

