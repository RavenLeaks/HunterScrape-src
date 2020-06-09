/*
 * Decompiled with CFR 0.145.
 */
package org.bson.io;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import org.bson.BsonSerializationException;
import org.bson.ByteBuf;
import org.bson.io.BsonInput;
import org.bson.io.BsonInputMark;
import org.bson.types.ObjectId;

public class ByteBufferBsonInput
implements BsonInput {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final String[] ONE_BYTE_ASCII_STRINGS = new String[128];
    private ByteBuf buffer;
    private int mark = -1;

    public ByteBufferBsonInput(ByteBuf buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer can not be null");
        }
        this.buffer = buffer;
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    @Override
    public int getPosition() {
        this.ensureOpen();
        return this.buffer.position();
    }

    @Override
    public byte readByte() {
        this.ensureOpen();
        this.ensureAvailable(1);
        return this.buffer.get();
    }

    @Override
    public void readBytes(byte[] bytes) {
        this.ensureOpen();
        this.ensureAvailable(bytes.length);
        this.buffer.get(bytes);
    }

    @Override
    public void readBytes(byte[] bytes, int offset, int length) {
        this.ensureOpen();
        this.ensureAvailable(length);
        this.buffer.get(bytes, offset, length);
    }

    @Override
    public long readInt64() {
        this.ensureOpen();
        this.ensureAvailable(8);
        return this.buffer.getLong();
    }

    @Override
    public double readDouble() {
        this.ensureOpen();
        this.ensureAvailable(8);
        return this.buffer.getDouble();
    }

    @Override
    public int readInt32() {
        this.ensureOpen();
        this.ensureAvailable(4);
        return this.buffer.getInt();
    }

    @Override
    public ObjectId readObjectId() {
        this.ensureOpen();
        byte[] bytes = new byte[12];
        this.readBytes(bytes);
        return new ObjectId(bytes);
    }

    @Override
    public String readString() {
        this.ensureOpen();
        int size = this.readInt32();
        if (size <= 0) {
            throw new BsonSerializationException(String.format("While decoding a BSON string found a size that is not a positive number: %d", size));
        }
        return this.readString(size);
    }

    @Override
    public String readCString() {
        this.ensureOpen();
        int mark = this.buffer.position();
        this.readUntilNullByte();
        int size = this.buffer.position() - mark;
        this.buffer.position(mark);
        return this.readString(size);
    }

    private String readString(int size) {
        if (size == 2) {
            byte asciiByte = this.readByte();
            byte nullByte = this.readByte();
            if (nullByte != 0) {
                throw new BsonSerializationException("Found a BSON string that is not null-terminated");
            }
            if (asciiByte < 0) {
                return UTF8_CHARSET.newDecoder().replacement();
            }
            return ONE_BYTE_ASCII_STRINGS[asciiByte];
        }
        byte[] bytes = new byte[size - 1];
        this.readBytes(bytes);
        byte nullByte = this.readByte();
        if (nullByte != 0) {
            throw new BsonSerializationException("Found a BSON string that is not null-terminated");
        }
        return new String(bytes, UTF8_CHARSET);
    }

    private void readUntilNullByte() {
        while (this.readByte() != 0) {
        }
    }

    @Override
    public void skipCString() {
        this.ensureOpen();
        this.readUntilNullByte();
    }

    @Override
    public void skip(int numBytes) {
        this.ensureOpen();
        this.buffer.position(this.buffer.position() + numBytes);
    }

    @Deprecated
    @Override
    public void mark(int readLimit) {
        this.ensureOpen();
        this.mark = this.buffer.position();
    }

    @Override
    public BsonInputMark getMark(int readLimit) {
        return new BsonInputMark(){
            private int mark;
            {
                this.mark = ByteBufferBsonInput.this.buffer.position();
            }

            @Override
            public void reset() {
                ByteBufferBsonInput.this.ensureOpen();
                ByteBufferBsonInput.this.buffer.position(this.mark);
            }
        };
    }

    @Deprecated
    @Override
    public void reset() {
        this.ensureOpen();
        if (this.mark == -1) {
            throw new IllegalStateException("Mark not set");
        }
        this.buffer.position(this.mark);
    }

    @Override
    public boolean hasRemaining() {
        this.ensureOpen();
        return this.buffer.hasRemaining();
    }

    @Override
    public void close() {
        this.buffer.release();
        this.buffer = null;
    }

    private void ensureOpen() {
        if (this.buffer == null) {
            throw new IllegalStateException("Stream is closed");
        }
    }

    private void ensureAvailable(int bytesNeeded) {
        if (this.buffer.remaining() < bytesNeeded) {
            throw new BsonSerializationException(String.format("While decoding a BSON document %d bytes were required, but only %d remain", bytesNeeded, this.buffer.remaining()));
        }
    }

    static {
        for (int b = 0; b < ONE_BYTE_ASCII_STRINGS.length; ++b) {
            ByteBufferBsonInput.ONE_BYTE_ASCII_STRINGS[b] = String.valueOf((char)b);
        }
    }

}

