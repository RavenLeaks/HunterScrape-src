/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import java.util.ArrayList;
import java.util.List;
import org.bson.BsonArray;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.assertions.Assertions;
import org.bson.codecs.BsonValueCodecProvider;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.Encoder;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

public class BsonArrayCodec
implements Codec<BsonArray> {
    private static final CodecRegistry DEFAULT_REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    private final CodecRegistry codecRegistry;

    public BsonArrayCodec() {
        this(DEFAULT_REGISTRY);
    }

    public BsonArrayCodec(CodecRegistry codecRegistry) {
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
    }

    @Override
    public BsonArray decode(BsonReader reader, DecoderContext decoderContext) {
        reader.readStartArray();
        ArrayList<BsonValue> list = new ArrayList<BsonValue>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(this.readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return new BsonArray(list);
    }

    @Override
    public void encode(BsonWriter writer, BsonArray array, EncoderContext encoderContext) {
        writer.writeStartArray();
        for (BsonValue value : array) {
            Codec<?> codec = this.codecRegistry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
        writer.writeEndArray();
    }

    @Override
    public Class<BsonArray> getEncoderClass() {
        return BsonArray.class;
    }

    protected BsonValue readValue(BsonReader reader, DecoderContext decoderContext) {
        return (BsonValue)this.codecRegistry.get(BsonValueCodecProvider.getClassForBsonType(reader.getCurrentBsonType())).decode(reader, decoderContext);
    }
}

