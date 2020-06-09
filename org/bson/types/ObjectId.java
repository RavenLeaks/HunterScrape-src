/*
 * Decompiled with CFR 0.145.
 */
package org.bson.types;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import org.bson.assertions.Assertions;

public final class ObjectId
implements Comparable<ObjectId>,
Serializable {
    private static final long serialVersionUID = 3670079982654483072L;
    private static final int OBJECT_ID_LENGTH = 12;
    private static final int LOW_ORDER_THREE_BYTES = 16777215;
    private static final int RANDOM_VALUE1;
    private static final short RANDOM_VALUE2;
    private static final AtomicInteger NEXT_COUNTER;
    private static final char[] HEX_CHARS;
    private final int timestamp;
    private final int counter;
    private final int randomValue1;
    private final short randomValue2;

    public static ObjectId get() {
        return new ObjectId();
    }

    public static boolean isValid(String hexString) {
        if (hexString == null) {
            throw new IllegalArgumentException();
        }
        int len = hexString.length();
        if (len != 24) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char c = hexString.charAt(i);
            if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F') continue;
            return false;
        }
        return true;
    }

    public ObjectId() {
        this(new Date());
    }

    public ObjectId(Date date) {
        this(ObjectId.dateToTimestampSeconds(date), NEXT_COUNTER.getAndIncrement() & 16777215, false);
    }

    public ObjectId(Date date, int counter) {
        this(ObjectId.dateToTimestampSeconds(date), counter, true);
    }

    @Deprecated
    public ObjectId(Date date, int machineIdentifier, short processIdentifier, int counter) {
        this(ObjectId.dateToTimestampSeconds(date), machineIdentifier, processIdentifier, counter);
    }

    @Deprecated
    public ObjectId(int timestamp, int machineIdentifier, short processIdentifier, int counter) {
        this(timestamp, machineIdentifier, processIdentifier, counter, true);
    }

    public ObjectId(int timestamp, int counter) {
        this(timestamp, counter, true);
    }

    private ObjectId(int timestamp, int counter, boolean checkCounter) {
        this(timestamp, RANDOM_VALUE1, RANDOM_VALUE2, counter, checkCounter);
    }

    private ObjectId(int timestamp, int randomValue1, short randomValue2, int counter, boolean checkCounter) {
        if ((randomValue1 & -16777216) != 0) {
            throw new IllegalArgumentException("The machine identifier must be between 0 and 16777215 (it must fit in three bytes).");
        }
        if (checkCounter && (counter & -16777216) != 0) {
            throw new IllegalArgumentException("The counter must be between 0 and 16777215 (it must fit in three bytes).");
        }
        this.timestamp = timestamp;
        this.counter = counter & 16777215;
        this.randomValue1 = randomValue1;
        this.randomValue2 = randomValue2;
    }

    public ObjectId(String hexString) {
        this(ObjectId.parseHexString(hexString));
    }

    public ObjectId(byte[] bytes) {
        this(ByteBuffer.wrap(Assertions.isTrueArgument("bytes has length of 12", bytes, Assertions.notNull("bytes", bytes).length == 12)));
    }

    ObjectId(int timestamp, int machineAndProcessIdentifier, int counter) {
        this(ObjectId.legacyToBytes(timestamp, machineAndProcessIdentifier, counter));
    }

    public ObjectId(ByteBuffer buffer) {
        Assertions.notNull("buffer", buffer);
        Assertions.isTrueArgument("buffer.remaining() >=12", buffer.remaining() >= 12);
        this.timestamp = ObjectId.makeInt(buffer.get(), buffer.get(), buffer.get(), buffer.get());
        this.randomValue1 = ObjectId.makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
        this.randomValue2 = ObjectId.makeShort(buffer.get(), buffer.get());
        this.counter = ObjectId.makeInt((byte)0, buffer.get(), buffer.get(), buffer.get());
    }

    private static byte[] legacyToBytes(int timestamp, int machineAndProcessIdentifier, int counter) {
        byte[] bytes = new byte[]{ObjectId.int3(timestamp), ObjectId.int2(timestamp), ObjectId.int1(timestamp), ObjectId.int0(timestamp), ObjectId.int3(machineAndProcessIdentifier), ObjectId.int2(machineAndProcessIdentifier), ObjectId.int1(machineAndProcessIdentifier), ObjectId.int0(machineAndProcessIdentifier), ObjectId.int3(counter), ObjectId.int2(counter), ObjectId.int1(counter), ObjectId.int0(counter)};
        return bytes;
    }

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        this.putToByteBuffer(buffer);
        return buffer.array();
    }

    public void putToByteBuffer(ByteBuffer buffer) {
        Assertions.notNull("buffer", buffer);
        Assertions.isTrueArgument("buffer.remaining() >=12", buffer.remaining() >= 12);
        buffer.put(ObjectId.int3(this.timestamp));
        buffer.put(ObjectId.int2(this.timestamp));
        buffer.put(ObjectId.int1(this.timestamp));
        buffer.put(ObjectId.int0(this.timestamp));
        buffer.put(ObjectId.int2(this.randomValue1));
        buffer.put(ObjectId.int1(this.randomValue1));
        buffer.put(ObjectId.int0(this.randomValue1));
        buffer.put(ObjectId.short1(this.randomValue2));
        buffer.put(ObjectId.short0(this.randomValue2));
        buffer.put(ObjectId.int2(this.counter));
        buffer.put(ObjectId.int1(this.counter));
        buffer.put(ObjectId.int0(this.counter));
    }

    public int getTimestamp() {
        return this.timestamp;
    }

    public Date getDate() {
        return new Date(((long)this.timestamp & 0xFFFFFFFFL) * 1000L);
    }

    public String toHexString() {
        char[] chars = new char[24];
        int i = 0;
        for (byte b : this.toByteArray()) {
            chars[i++] = HEX_CHARS[b >> 4 & 15];
            chars[i++] = HEX_CHARS[b & 15];
        }
        return new String(chars);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        ObjectId objectId = (ObjectId)o;
        if (this.counter != objectId.counter) {
            return false;
        }
        if (this.timestamp != objectId.timestamp) {
            return false;
        }
        if (this.randomValue1 != objectId.randomValue1) {
            return false;
        }
        return this.randomValue2 == objectId.randomValue2;
    }

    public int hashCode() {
        int result = this.timestamp;
        result = 31 * result + this.counter;
        result = 31 * result + this.randomValue1;
        result = 31 * result + this.randomValue2;
        return result;
    }

    @Override
    public int compareTo(ObjectId other) {
        if (other == null) {
            throw new NullPointerException();
        }
        byte[] byteArray = this.toByteArray();
        byte[] otherByteArray = other.toByteArray();
        for (int i = 0; i < 12; ++i) {
            if (byteArray[i] == otherByteArray[i]) continue;
            return (byteArray[i] & 255) < (otherByteArray[i] & 255) ? -1 : 1;
        }
        return 0;
    }

    public String toString() {
        return this.toHexString();
    }

    @Deprecated
    public static ObjectId createFromLegacyFormat(int time, int machine, int inc) {
        return new ObjectId(time, machine, inc);
    }

    @Deprecated
    public static int getCurrentCounter() {
        return NEXT_COUNTER.get() & 16777215;
    }

    @Deprecated
    public static int getGeneratedMachineIdentifier() {
        return RANDOM_VALUE1;
    }

    @Deprecated
    public static int getGeneratedProcessIdentifier() {
        return RANDOM_VALUE2;
    }

    @Deprecated
    public int getMachineIdentifier() {
        return this.randomValue1;
    }

    @Deprecated
    public short getProcessIdentifier() {
        return this.randomValue2;
    }

    @Deprecated
    public int getCounter() {
        return this.counter;
    }

    @Deprecated
    public int getTimeSecond() {
        return this.timestamp;
    }

    @Deprecated
    public long getTime() {
        return ((long)this.timestamp & 0xFFFFFFFFL) * 1000L;
    }

    @Deprecated
    public String toStringMongod() {
        return this.toHexString();
    }

    private static byte[] parseHexString(String s) {
        if (!ObjectId.isValid(s)) {
            throw new IllegalArgumentException("invalid hexadecimal representation of an ObjectId: [" + s + "]");
        }
        byte[] b = new byte[12];
        for (int i = 0; i < b.length; ++i) {
            b[i] = (byte)Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        return b;
    }

    private static int dateToTimestampSeconds(Date time) {
        return (int)(time.getTime() / 1000L);
    }

    private static int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return b3 << 24 | (b2 & 255) << 16 | (b1 & 255) << 8 | b0 & 255;
    }

    private static short makeShort(byte b1, byte b0) {
        return (short)((b1 & 255) << 8 | b0 & 255);
    }

    private static byte int3(int x) {
        return (byte)(x >> 24);
    }

    private static byte int2(int x) {
        return (byte)(x >> 16);
    }

    private static byte int1(int x) {
        return (byte)(x >> 8);
    }

    private static byte int0(int x) {
        return (byte)x;
    }

    private static byte short1(short x) {
        return (byte)(x >> 8);
    }

    private static byte short0(short x) {
        return (byte)x;
    }

    static {
        NEXT_COUNTER = new AtomicInteger(new SecureRandom().nextInt());
        HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            SecureRandom secureRandom = new SecureRandom();
            RANDOM_VALUE1 = secureRandom.nextInt(16777216);
            RANDOM_VALUE2 = (short)secureRandom.nextInt(32768);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

