/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson;

import com.mongodb.client.model.geojson.CoordinateReferenceSystem;
import com.mongodb.client.model.geojson.GeoJsonObjectType;
import com.mongodb.client.model.geojson.codecs.GeoJsonCodecProvider;
import com.mongodb.lang.Nullable;
import java.io.StringWriter;
import java.io.Writer;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.json.JsonWriter;
import org.bson.json.JsonWriterSettings;

public abstract class Geometry {
    private static final CodecRegistry REGISTRY = CodecRegistries.fromProviders(new GeoJsonCodecProvider());
    private final CoordinateReferenceSystem coordinateReferenceSystem;

    protected Geometry() {
        this(null);
    }

    protected Geometry(@Nullable CoordinateReferenceSystem coordinateReferenceSystem) {
        this.coordinateReferenceSystem = coordinateReferenceSystem;
    }

    public abstract GeoJsonObjectType getType();

    public String toJson() {
        StringWriter stringWriter = new StringWriter();
        JsonWriter writer = new JsonWriter(stringWriter, new JsonWriterSettings());
        Codec<?> codec = Geometry.getRegistry().get(this.getClass());
        codec.encode(writer, this, EncoderContext.builder().build());
        return stringWriter.toString();
    }

    static CodecRegistry getRegistry() {
        return REGISTRY;
    }

    @Nullable
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return this.coordinateReferenceSystem;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Geometry geometry = (Geometry)o;
        return !(this.coordinateReferenceSystem != null ? !this.coordinateReferenceSystem.equals(geometry.coordinateReferenceSystem) : geometry.coordinateReferenceSystem != null);
    }

    public int hashCode() {
        return this.coordinateReferenceSystem != null ? this.coordinateReferenceSystem.hashCode() : 0;
    }
}

