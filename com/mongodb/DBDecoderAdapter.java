/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBCollection;
import com.mongodb.DBDecoder;
import com.mongodb.DBObject;
import com.mongodb.MongoInternalException;
import com.mongodb.connection.BufferProvider;
import com.mongodb.connection.ByteBufferBsonOutput;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.bson.BsonBinaryWriter;
import org.bson.BsonReader;
import org.bson.codecs.Decoder;
import org.bson.codecs.DecoderContext;
import org.bson.io.BsonOutput;

class DBDecoderAdapter
implements Decoder<DBObject> {
    private final DBDecoder decoder;
    private final DBCollection collection;
    private final BufferProvider bufferProvider;

    DBDecoderAdapter(DBDecoder decoder, DBCollection collection, BufferProvider bufferProvider) {
        this.decoder = decoder;
        this.collection = collection;
        this.bufferProvider = bufferProvider;
    }

    @Override
    public DBObject decode(BsonReader reader, DecoderContext decoderContext) {
        ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(this.bufferProvider);
        BsonBinaryWriter binaryWriter = new BsonBinaryWriter(bsonOutput);
        try {
            binaryWriter.pipe(reader);
            BufferExposingByteArrayOutputStream byteArrayOutputStream = new BufferExposingByteArrayOutputStream(binaryWriter.getBsonOutput().getSize());
            bsonOutput.pipe(byteArrayOutputStream);
            DBObject dBObject = this.decoder.decode(byteArrayOutputStream.getInternalBytes(), this.collection);
            return dBObject;
        }
        catch (IOException e) {
            throw new MongoInternalException("An unlikely IOException thrown.", e);
        }
        finally {
            binaryWriter.close();
            bsonOutput.close();
        }
    }

    private static class BufferExposingByteArrayOutputStream
    extends ByteArrayOutputStream {
        BufferExposingByteArrayOutputStream(int size) {
            super(size);
        }

        byte[] getInternalBytes() {
            return this.buf;
        }
    }

}

