/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.assertions.Assertions;
import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.PolygonCoordinates;
import com.mongodb.client.model.geojson.Position;
import com.mongodb.lang.Nullable;
import java.util.List;

public final class Polygon
extends Geometry {
    private final PolygonCoordinates coordinates;

    public Polygon(List<Position> exterior, List<Position> ... holes) {
        this(new PolygonCoordinates(exterior, holes));
    }

    public Polygon(PolygonCoordinates coordinates) {
        this(null, coordinates);
    }

    public Polygon(@Nullable CoordinateReferenceSystem coordinateReferenceSystem, PolygonCoordinates coordinates) {
        super(coordinateReferenceSystem);
        this.coordinates = Assertions.notNull("coordinates", coordinates);
    }

    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.POLYGON;
    }

    public PolygonCoordinates getCoordinates() {
        return this.coordinates;
    }

    public List<Position> getExterior() {
        return this.coordinates.getExterior();
    }

    public List<List<Position>> getHoles() {
        return this.coordinates.getHoles();
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
        Polygon polygon = (Polygon)o;
        return this.coordinates.equals(polygon.coordinates);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        return result;
    }

    public String toString() {
        return "Polygon{exterior=" + this.coordinates.getExterior() + (this.coordinates.getHoles().isEmpty() ? "" : ", holes=" + this.coordinates.getHoles()) + (this.getCoordinateReferenceSystem() == null ? "" : ", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem()) + '}';
    }
}

