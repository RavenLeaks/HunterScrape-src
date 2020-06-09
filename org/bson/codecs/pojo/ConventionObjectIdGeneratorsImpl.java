/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import org.bson.BsonObjectId;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.Convention;
import org.bson.codecs.pojo.IdGenerator;
import org.bson.codecs.pojo.IdGenerators;
import org.bson.codecs.pojo.PropertyModelBuilder;
import org.bson.codecs.pojo.TypeData;
import org.bson.types.ObjectId;

final class ConventionObjectIdGeneratorsImpl
implements Convention {
    ConventionObjectIdGeneratorsImpl() {
    }

    @Override
    public void apply(ClassModelBuilder<?> classModelBuilder) {
        PropertyModelBuilder<?> idProperty;
        if (classModelBuilder.getIdGenerator() == null && classModelBuilder.getIdPropertyName() != null && (idProperty = classModelBuilder.getProperty(classModelBuilder.getIdPropertyName())) != null) {
            Class<?> idType = idProperty.getTypeData().getType();
            if (classModelBuilder.getIdGenerator() == null && idType.equals(ObjectId.class)) {
                classModelBuilder.idGenerator(IdGenerators.OBJECT_ID_GENERATOR);
            } else if (classModelBuilder.getIdGenerator() == null && idType.equals(BsonObjectId.class)) {
                classModelBuilder.idGenerator(IdGenerators.BSON_OBJECT_ID_GENERATOR);
            }
        }
    }
}

