/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.assertions.Assertions;

final class ChangeEvent<T> {
    private final T previousValue;
    private final T newValue;

    ChangeEvent(T previousValue, T newValue) {
        this.previousValue = Assertions.notNull("oldValue", previousValue);
        this.newValue = Assertions.notNull("newValue", newValue);
    }

    public T getPreviousValue() {
        return this.previousValue;
    }

    public T getNewValue() {
        return this.newValue;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ChangeEvent that = (ChangeEvent)o;
        if (!this.newValue.equals(that.newValue)) {
            return false;
        }
        return !(this.previousValue != null ? !this.previousValue.equals(that.previousValue) : that.previousValue != null);
    }

    public int hashCode() {
        int result = this.previousValue != null ? this.previousValue.hashCode() : 0;
        result = 31 * result + this.newValue.hashCode();
        return result;
    }

    public String toString() {
        return "ChangeEvent{previousValue=" + this.previousValue + ", newValue=" + this.newValue + '}';
    }
}

