/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

import com.mongodb.assertions.Assertions;

public class Variable<TExpression> {
    private final String name;
    private final TExpression value;

    public Variable(String name, TExpression value) {
        this.name = Assertions.notNull("name", name);
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public TExpression getValue() {
        return this.value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Variable)) {
            return false;
        }
        Variable variable = (Variable)o;
        if (!this.name.equals(variable.name)) {
            return false;
        }
        return this.value != null ? this.value.equals(variable.value) : variable.value == null;
    }

    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Variable{name='" + this.name + '\'' + ", value=" + this.value + '}';
    }
}

