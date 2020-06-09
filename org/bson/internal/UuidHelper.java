/*
 * Decompiled with CFR 0.145.
 */
package org.bson.internal;

import java.util.UUID;
import org.bson.BSONException;
import org.bson.BsonBinarySubType;
import org.bson.BsonSerializationException;
import org.bson.UuidRepresentation;

public final class UuidHelper {
    private static void writeLongToArrayBigEndian(byte[] bytes, int offset, long x) {
        bytes[offset + 7] = (byte)(255L & x);
        bytes[offset + 6] = (byte)(255L & x >> 8);
        bytes[offset + 5] = (byte)(255L & x >> 16);
        bytes[offset + 4] = (byte)(255L & x >> 24);
        bytes[offset + 3] = (byte)(255L & x >> 32);
        bytes[offset + 2] = (byte)(255L & x >> 40);
        bytes[offset + 1] = (byte)(255L & x >> 48);
        bytes[offset] = (byte)(255L & x >> 56);
    }

    private static long readLongFromArrayBigEndian(byte[] bytes, int offset) {
        long x = 0L;
        x |= 255L & (long)bytes[offset + 7];
        x |= (255L & (long)bytes[offset + 6]) << 8;
        x |= (255L & (long)bytes[offset + 5]) << 16;
        x |= (255L & (long)bytes[offset + 4]) << 24;
        x |= (255L & (long)bytes[offset + 3]) << 32;
        x |= (255L & (long)bytes[offset + 2]) << 40;
        x |= (255L & (long)bytes[offset + 1]) << 48;
        return x |= (255L & (long)bytes[offset]) << 56;
    }

    private static void reverseByteArray(byte[] data, int start, int length) {
        int left = start;
        for (int right = start + length - 1; left < right; ++left, --right) {
            byte temp = data[left];
            data[left] = data[right];
            data[right] = temp;
        }
    }

    public static byte[] encodeUuidToBinary(UUID uuid, UuidRepresentation uuidRepresentation) {
        byte[] binaryData = new byte[16];
        UuidHelper.writeLongToArrayBigEndian(binaryData, 0, uuid.getMostSignificantBits());
        UuidHelper.writeLongToArrayBigEndian(binaryData, 8, uuid.getLeastSignificantBits());
        switch (uuidRepresentation) {
            case C_SHARP_LEGACY: {
                UuidHelper.reverseByteArray(binaryData, 0, 4);
                UuidHelper.reverseByteArray(binaryData, 4, 2);
                UuidHelper.reverseByteArray(binaryData, 6, 2);
                break;
            }
            case JAVA_LEGACY: {
                UuidHelper.reverseByteArray(binaryData, 0, 8);
                UuidHelper.reverseByteArray(binaryData, 8, 8);
                break;
            }
            case PYTHON_LEGACY: 
            case STANDARD: {
                break;
            }
            default: {
                throw new BSONException("Unexpected UUID representation");
            }
        }
        return binaryData;
    }

    public static UUID decodeBinaryToUuid(byte[] data, byte type, UuidRepresentation uuidRepresentation) {
        if (data.length != 16) {
            throw new BsonSerializationException(String.format("Expected length to be 16, not %d.", data.length));
        }
        if (type == BsonBinarySubType.UUID_LEGACY.getValue()) {
            switch (uuidRepresentation) {
                case C_SHARP_LEGACY: {
                    UuidHelper.reverseByteArray(data, 0, 4);
                    UuidHelper.reverseByteArray(data, 4, 2);
                    UuidHelper.reverseByteArray(data, 6, 2);
                    break;
                }
                case JAVA_LEGACY: {
                    UuidHelper.reverseByteArray(data, 0, 8);
                    UuidHelper.reverseByteArray(data, 8, 8);
                    break;
                }
                case PYTHON_LEGACY: 
                case STANDARD: {
                    break;
                }
                default: {
                    throw new BSONException("Unexpected UUID representation");
                }
            }
        }
        return new UUID(UuidHelper.readLongFromArrayBigEndian(data, 0), UuidHelper.readLongFromArrayBigEndian(data, 8));
    }

    private UuidHelper() {
    }

}

