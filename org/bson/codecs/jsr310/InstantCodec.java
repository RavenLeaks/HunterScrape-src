/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs.jsr310;

import java.time.Instant;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecConfigurationException;
import org.bson.codecs.jsr310.DateTimeBasedCodec;

public class InstantCodec
extends DateTimeBasedCodec<Instant> {
    @Override
    public Instant decode(BsonReader reader, DecoderContext decoderContext) {
        return Instant.ofEpochMilli(this.validateAndReadDateTime(reader));
    }

    @Override
    public void encode(BsonWriter writer, Instant value, EncoderContext encoderContext) {
        try {
            writer.writeDateTime(value.toEpochMilli());
        }
        catch (ArithmeticException e) {
            throw new CodecConfigurationException(String.format("Unsupported Instant value '%s' could not be converted to milliseconds: %s", value, e.getMessage()), e);
        }
    }

    @Override
    public Class<Instant> getEncoderClass() {
        return Instant.class;
    }
}

