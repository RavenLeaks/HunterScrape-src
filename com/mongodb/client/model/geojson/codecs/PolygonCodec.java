/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.client.model.geojson.codecs;

import com.mongodb.client.model.geojson.Geometry;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.codecs.AbstractGeometryCodec;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

public class PolygonCodec
extends AbstractGeometryCodec<Polygon> {
    public PolygonCodec(CodecRegistry registry) {
        super(registry, Polygon.class);
    }
}

