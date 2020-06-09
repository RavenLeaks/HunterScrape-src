/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.assertions.Assertions;
import com.mongodb.client.model.geojson.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PolygonCoordinates {
    private final List<Position> exterior;
    private final List<List<Position>> holes;

    public PolygonCoordinates(List<Position> exterior, List<Position> ... holes) {
        Assertions.notNull("exteriorRing", exterior);
        Assertions.isTrueArgument("ring contains only non-null positions", !exterior.contains(null));
        Assertions.isTrueArgument("ring must contain at least four positions", exterior.size() >= 4);
        Assertions.isTrueArgument("first and last position must be the same", exterior.get(0).equals(exterior.get(exterior.size() - 1)));
        this.exterior = Collections.unmodifiableList(exterior);
        ArrayList<List<Position>> holesList = new ArrayList<List<Position>>(holes.length);
        for (List<Position> hole : holes) {
            Assertions.notNull("interiorRing", hole);
            Assertions.isTrueArgument("ring contains only non-null positions", !hole.contains(null));
            Assertions.isTrueArgument("ring must contain at least four positions", hole.size() >= 4);
            Assertions.isTrueArgument("first and last position must be the same", hole.get(0).equals(hole.get(hole.size() - 1)));
            holesList.add(Collections.unmodifiableList(hole));
        }
        this.holes = Collections.unmodifiableList(holesList);
    }

    public List<Position> getExterior() {
        return this.exterior;
    }

    public List<List<Position>> getHoles() {
        return this.holes;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        PolygonCoordinates that = (PolygonCoordinates)o;
        if (!this.exterior.equals(that.exterior)) {
            return false;
        }
        return this.holes.equals(that.holes);
    }

    public int hashCode() {
        int result = this.exterior.hashCode();
        result = 31 * result + this.holes.hashCode();
        return result;
    }

    public String toString() {
        return "PolygonCoordinates{exterior=" + this.exterior + (this.holes.isEmpty() ? "" : ", holes=" + this.holes) + '}';
    }
}

