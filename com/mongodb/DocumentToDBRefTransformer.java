/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBRef;
import org.bson.Document;
import org.bson.Transformer;

public final class DocumentToDBRefTransformer
implements Transformer {
    @Override
    public Object transform(Object value) {
        Document document;
        if (value instanceof Document && (document = (Document)value).containsKey("$id") && document.containsKey("$ref")) {
            return new DBRef((String)document.get("$db"), (String)document.get("$ref"), document.get("$id"));
        }
        return value;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        return o != null && this.getClass() == o.getClass();
    }

    public int hashCode() {
        return 0;
    }
}

