/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import com.mongodb.connection.BufferProvider;
import com.mongodb.internal.connection.ConcurrentPool;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import org.bson.ByteBuf;
import org.bson.ByteBufNIO;

public class PowerOfTwoBufferPool
implements BufferProvider {
    private final Map<Integer, ConcurrentPool<ByteBuffer>> powerOfTwoToPoolMap = new HashMap<Integer, ConcurrentPool<ByteBuffer>>();

    public PowerOfTwoBufferPool() {
        this(24);
    }

    public PowerOfTwoBufferPool(int highestPowerOfTwo) {
        int powerOfTwo = 1;
        for (int i = 0; i <= highestPowerOfTwo; ++i) {
            final int size = powerOfTwo;
            this.powerOfTwoToPoolMap.put(i, new ConcurrentPool<ByteBuffer>(Integer.MAX_VALUE, new ConcurrentPool.ItemFactory<ByteBuffer>(){

                @Override
                public ByteBuffer create(boolean initialize) {
                    return PowerOfTwoBufferPool.this.createNew(size);
                }

                @Override
                public void close(ByteBuffer byteBuffer) {
                }

                @Override
                public ConcurrentPool.Prune shouldPrune(ByteBuffer byteBuffer) {
                    return ConcurrentPool.Prune.STOP;
                }
            }));
            powerOfTwo <<= 1;
        }
    }

    @Override
    public ByteBuf getBuffer(int size) {
        ConcurrentPool<ByteBuffer> pool = this.powerOfTwoToPoolMap.get(PowerOfTwoBufferPool.log2(PowerOfTwoBufferPool.roundUpToNextHighestPowerOfTwo(size)));
        ByteBuffer byteBuffer = pool == null ? this.createNew(size) : pool.get();
        ((Buffer)byteBuffer).clear();
        ((Buffer)byteBuffer).limit(size);
        return new PooledByteBufNIO(byteBuffer);
    }

    private ByteBuffer createNew(int size) {
        ByteBuffer buf = ByteBuffer.allocate(size);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        return buf;
    }

    private void release(ByteBuffer buffer) {
        ConcurrentPool<ByteBuffer> pool = this.powerOfTwoToPoolMap.get(PowerOfTwoBufferPool.log2(PowerOfTwoBufferPool.roundUpToNextHighestPowerOfTwo(buffer.capacity())));
        if (pool != null) {
            pool.release(buffer);
        }
    }

    static int log2(int powerOfTwo) {
        return 31 - Integer.numberOfLeadingZeros(powerOfTwo);
    }

    static int roundUpToNextHighestPowerOfTwo(int size) {
        int v = size;
        --v;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return ++v;
    }

    private class PooledByteBufNIO
    extends ByteBufNIO {
        PooledByteBufNIO(ByteBuffer buf) {
            super(buf);
        }

        @Override
        public void release() {
            ByteBuffer wrapped = this.asNIO();
            super.release();
            if (this.getReferenceCount() == 0) {
                PowerOfTwoBufferPool.this.release(wrapped);
            }
        }
    }

}

