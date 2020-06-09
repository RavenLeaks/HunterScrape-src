/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.bson.BSONCallback;
import org.bson.BSONCallbackAdapter;
import org.bson.BSONDecoder;
import org.bson.BSONObject;
import org.bson.BasicBSONCallback;
import org.bson.BsonBinaryReader;
import org.bson.BsonReader;
import org.bson.BsonWriterSettings;
import org.bson.ByteBuf;
import org.bson.ByteBufNIO;
import org.bson.io.Bits;
import org.bson.io.BsonInput;
import org.bson.io.ByteBufferBsonInput;

public class BasicBSONDecoder
implements BSONDecoder {
    @Override
    public BSONObject readObject(byte[] bytes) {
        BasicBSONCallback bsonCallback = new BasicBSONCallback();
        this.decode(bytes, (BSONCallback)bsonCallback);
        return (BSONObject)bsonCallback.get();
    }

    @Override
    public BSONObject readObject(InputStream in) throws IOException {
        return this.readObject(this.readFully(in));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public int decode(byte[] bytes, BSONCallback callback) {
        BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(ByteBuffer.wrap(bytes))));
        try {
            BSONCallbackAdapter writer = new BSONCallbackAdapter(new BsonWriterSettings(), callback);
            writer.pipe(reader);
            int n = reader.getBsonInput().getPosition();
            return n;
        }
        finally {
            reader.close();
        }
    }

    @Override
    public int decode(InputStream in, BSONCallback callback) throws IOException {
        return this.decode(this.readFully(in), callback);
    }

    private byte[] readFully(InputStream input) throws IOException {
        byte[] sizeBytes = new byte[4];
        Bits.readFully(input, sizeBytes);
        int size = Bits.readInt(sizeBytes);
        byte[] buffer = new byte[size];
        System.arraycopy(sizeBytes, 0, buffer, 0, 4);
        Bits.readFully(input, buffer, 4, size - 4);
        return buffer;
    }
}

