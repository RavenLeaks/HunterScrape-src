/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.pojo;

import java.lang.reflect.TypeVariable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bson.codecs.pojo.ClassModelBuilder;
import org.bson.codecs.pojo.IdPropertyModelHolder;
import org.bson.codecs.pojo.InstanceCreator;
import org.bson.codecs.pojo.InstanceCreatorFactory;
import org.bson.codecs.pojo.PropertyModel;
import org.bson.codecs.pojo.TypeParameterMap;

public final class ClassModel<T> {
    private final String name;
    private final Class<T> type;
    private final boolean hasTypeParameters;
    private final InstanceCreatorFactory<T> instanceCreatorFactory;
    private final boolean discriminatorEnabled;
    private final String discriminatorKey;
    private final String discriminator;
    private final IdPropertyModelHolder<?> idPropertyModelHolder;
    private final List<PropertyModel<?>> propertyModels;
    private final Map<String, TypeParameterMap> propertyNameToTypeParameterMap;

    ClassModel(Class<T> clazz, Map<String, TypeParameterMap> propertyNameToTypeParameterMap, InstanceCreatorFactory<T> instanceCreatorFactory, Boolean discriminatorEnabled, String discriminatorKey, String discriminator, IdPropertyModelHolder<?> idPropertyModelHolder, List<PropertyModel<?>> propertyModels) {
        this.name = clazz.getSimpleName();
        this.type = clazz;
        this.hasTypeParameters = clazz.getTypeParameters().length > 0;
        this.propertyNameToTypeParameterMap = Collections.unmodifiableMap(propertyNameToTypeParameterMap);
        this.instanceCreatorFactory = instanceCreatorFactory;
        this.discriminatorEnabled = discriminatorEnabled;
        this.discriminatorKey = discriminatorKey;
        this.discriminator = discriminator;
        this.idPropertyModelHolder = idPropertyModelHolder;
        this.propertyModels = Collections.unmodifiableList(propertyModels);
    }

    public static <S> ClassModelBuilder<S> builder(Class<S> type) {
        return new ClassModelBuilder<S>(type);
    }

    InstanceCreator<T> getInstanceCreator() {
        return this.instanceCreatorFactory.create();
    }

    public Class<T> getType() {
        return this.type;
    }

    public boolean hasTypeParameters() {
        return this.hasTypeParameters;
    }

    public boolean useDiscriminator() {
        return this.discriminatorEnabled;
    }

    public String getDiscriminatorKey() {
        return this.discriminatorKey;
    }

    public String getDiscriminator() {
        return this.discriminator;
    }

    public PropertyModel<?> getPropertyModel(String propertyName) {
        for (PropertyModel<?> propertyModel : this.propertyModels) {
            if (!propertyModel.getName().equals(propertyName)) continue;
            return propertyModel;
        }
        return null;
    }

    public List<PropertyModel<?>> getPropertyModels() {
        return this.propertyModels;
    }

    public PropertyModel<?> getIdPropertyModel() {
        return this.idPropertyModelHolder != null ? this.idPropertyModelHolder.getPropertyModel() : null;
    }

    IdPropertyModelHolder<?> getIdPropertyModelHolder() {
        return this.idPropertyModelHolder;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return "ClassModel{type=" + this.type + "}";
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ClassModel that = (ClassModel)o;
        if (this.discriminatorEnabled != that.discriminatorEnabled) {
            return false;
        }
        if (!this.getType().equals(that.getType())) {
            return false;
        }
        if (!this.getInstanceCreatorFactory().equals(that.getInstanceCreatorFactory())) {
            return false;
        }
        if (this.getDiscriminatorKey() != null ? !this.getDiscriminatorKey().equals(that.getDiscriminatorKey()) : that.getDiscriminatorKey() != null) {
            return false;
        }
        if (this.getDiscriminator() != null ? !this.getDiscriminator().equals(that.getDiscriminator()) : that.getDiscriminator() != null) {
            return false;
        }
        if (this.idPropertyModelHolder != null ? !this.idPropertyModelHolder.equals(that.idPropertyModelHolder) : that.idPropertyModelHolder != null) {
            return false;
        }
        if (!this.getPropertyModels().equals(that.getPropertyModels())) {
            return false;
        }
        return this.getPropertyNameToTypeParameterMap().equals(that.getPropertyNameToTypeParameterMap());
    }

    public int hashCode() {
        int result = this.getType().hashCode();
        result = 31 * result + this.getInstanceCreatorFactory().hashCode();
        result = 31 * result + (this.discriminatorEnabled ? 1 : 0);
        result = 31 * result + (this.getDiscriminatorKey() != null ? this.getDiscriminatorKey().hashCode() : 0);
        result = 31 * result + (this.getDiscriminator() != null ? this.getDiscriminator().hashCode() : 0);
        result = 31 * result + (this.getIdPropertyModelHolder() != null ? this.getIdPropertyModelHolder().hashCode() : 0);
        result = 31 * result + this.getPropertyModels().hashCode();
        result = 31 * result + this.getPropertyNameToTypeParameterMap().hashCode();
        return result;
    }

    InstanceCreatorFactory<T> getInstanceCreatorFactory() {
        return this.instanceCreatorFactory;
    }

    Map<String, TypeParameterMap> getPropertyNameToTypeParameterMap() {
        return this.propertyNameToTypeParameterMap;
    }
}

