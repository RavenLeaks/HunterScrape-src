/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.changestream;

import com.mongodb.lang.Nullable;
import java.util.List;
import org.bson.BsonDocument;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;

public final class UpdateDescription {
    private final List<String> removedFields;
    private final BsonDocument updatedFields;

    @BsonCreator
    public UpdateDescription(@Nullable @BsonProperty(value="removedFields") List<String> removedFields, @Nullable @BsonProperty(value="updatedFields") BsonDocument updatedFields) {
        this.removedFields = removedFields;
        this.updatedFields = updatedFields;
    }

    @Nullable
    public List<String> getRemovedFields() {
        return this.removedFields;
    }

    @Nullable
    public BsonDocument getUpdatedFields() {
        return this.updatedFields;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        UpdateDescription that = (UpdateDescription)o;
        if (this.removedFields != null ? !this.removedFields.equals(that.getRemovedFields()) : that.getRemovedFields() != null) {
            return false;
        }
        return !(this.updatedFields != null ? !this.updatedFields.equals(that.getUpdatedFields()) : that.getUpdatedFields() != null);
    }

    public int hashCode() {
        int result = this.removedFields != null ? this.removedFields.hashCode() : 0;
        result = 31 * result + (this.updatedFields != null ? this.updatedFields.hashCode() : 0);
        return result;
    }

    public String toString() {
        return "UpdateDescription{removedFields=" + this.removedFields + ", updatedFields=" + this.updatedFields + "}";
    }
}

