/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model;

import com.mongodb.client.model.BuildersHelper;
import org.bson.BsonDocument;
import org.bson.BsonDocumentWriter;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

class SimpleExpression<TExpression>
implements Bson {
    private final String name;
    private final TExpression expression;

    SimpleExpression(String name, TExpression expression) {
        this.name = name;
        this.expression = expression;
    }

    @Override
    public <TDocument> BsonDocument toBsonDocument(Class<TDocument> documentClass, CodecRegistry codecRegistry) {
        BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
        writer.writeStartDocument();
        writer.writeName(this.name);
        BuildersHelper.encodeValue(writer, this.expression, codecRegistry);
        writer.writeEndDocument();
        return writer.getDocument();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        SimpleExpression that = (SimpleExpression)o;
        if (this.name != null ? !this.name.equals(that.name) : that.name != null) {
            return false;
        }
        return this.expression != null ? this.expression.equals(that.expression) : that.expression == null;
    }

    public int hashCode() {
        int result = this.name != null ? this.name.hashCode() : 0;
        result = 31 * result + (this.expression != null ? this.expression.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "Expression{name='" + this.name + '\'' + ", expression=" + this.expression + '}';
    }
}

