/*
 * Decompiled with CFR 0.145.
 */
package org.bson.types;

import org.bson.BSONObject;
import org.bson.types.Code;

public class CodeWScope
extends Code {
    private final BSONObject scope;
    private static final long serialVersionUID = -6284832275113680002L;

    public CodeWScope(String code, BSONObject scope) {
        super(code);
        this.scope = scope;
    }

    public BSONObject getScope() {
        return this.scope;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        CodeWScope c = (CodeWScope)o;
        return this.getCode().equals(c.getCode()) && this.scope.equals(c.scope);
    }

    @Override
    public int hashCode() {
        return this.getCode().hashCode() ^ this.scope.hashCode();
    }
}

