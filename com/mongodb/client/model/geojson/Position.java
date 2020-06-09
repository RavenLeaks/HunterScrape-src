/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.annotations.Immutable;
import com.mongodb.assertions.Assertions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Immutable
public final class Position {
    private final List<Double> values;

    public Position(List<Double> values) {
        Assertions.notNull("values", values);
        Assertions.isTrueArgument("value contains only non-null elements", !values.contains(null));
        Assertions.isTrueArgument("value must contain at least two elements", values.size() >= 2);
        this.values = Collections.unmodifiableList(values);
    }

    public Position(double first, double second, double ... remaining) {
        ArrayList<Double> values = new ArrayList<Double>();
        values.add(first);
        values.add(second);
        for (double cur : remaining) {
            values.add(cur);
        }
        this.values = Collections.unmodifiableList(values);
    }

    public List<Double> getValues() {
        return this.values;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Position that = (Position)o;
        return this.values.equals(that.values);
    }

    public int hashCode() {
        return this.values.hashCode();
    }

    public String toString() {
        return "Position{values=" + this.values + '}';
    }
}

