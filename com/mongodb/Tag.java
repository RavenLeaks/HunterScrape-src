/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.annotations.Immutable;
import com.mongodb.assertions.Assertions;

@Immutable
public final class Tag {
    private final String name;
    private final String value;

    public Tag(String name, String value) {
        this.name = Assertions.notNull("name", name);
        this.value = Assertions.notNull("value", value);
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Tag that = (Tag)o;
        if (!this.name.equals(that.name)) {
            return false;
        }
        return this.value.equals(that.value);
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.value.hashCode();
        return result;
    }

    public String toString() {
        return "Tag{name='" + this.name + '\'' + ", value='" + this.value + '\'' + '}';
    }
}

