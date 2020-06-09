/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.SplittablePayload;
import com.mongodb.internal.connection.BsonWriterHelper;
import com.mongodb.internal.connection.LevelCountingBsonWriter;
import com.mongodb.internal.connection.MessageSettings;
import java.util.List;
import org.bson.BsonBinaryWriter;
import org.bson.BsonDocument;
import org.bson.BsonWriter;
import org.bson.io.BsonOutput;

public class SplittablePayloadBsonWriter
extends LevelCountingBsonWriter {
    private final BsonWriter writer;
    private final BsonOutput bsonOutput;
    private final SplittablePayload payload;
    private final MessageSettings settings;
    private final int messageStartPosition;

    public SplittablePayloadBsonWriter(BsonBinaryWriter writer, BsonOutput bsonOutput, MessageSettings settings, SplittablePayload payload) {
        this(writer, bsonOutput, 0, settings, payload);
    }

    public SplittablePayloadBsonWriter(BsonBinaryWriter writer, BsonOutput bsonOutput, int messageStartPosition, MessageSettings settings, SplittablePayload payload) {
        super(writer);
        this.writer = writer;
        this.bsonOutput = bsonOutput;
        this.messageStartPosition = messageStartPosition;
        this.settings = settings;
        this.payload = payload;
    }

    @Override
    public void writeStartDocument() {
        super.writeStartDocument();
    }

    @Override
    public void writeEndDocument() {
        if (this.getCurrentLevel() == 0 && this.payload.getPayload().size() > 0) {
            BsonWriterHelper.writePayloadArray(this.writer, this.bsonOutput, this.settings, this.messageStartPosition, this.payload);
        }
        super.writeEndDocument();
    }
}

