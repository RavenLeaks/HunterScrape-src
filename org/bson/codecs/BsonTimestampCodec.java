/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import org.bson.BsonReader;
import org.bson.BsonTimestamp;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

public class BsonTimestampCodec
implements Codec<BsonTimestamp> {
    @Override
    public void encode(BsonWriter writer, BsonTimestamp value, EncoderContext encoderContext) {
        writer.writeTimestamp(value);
    }

    @Override
    public BsonTimestamp decode(BsonReader reader, DecoderContext decoderContext) {
        return reader.readTimestamp();
    }

    @Override
    public Class<BsonTimestamp> getEncoderClass() {
        return BsonTimestamp.class;
    }
}

