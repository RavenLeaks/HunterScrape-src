/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class GridFSDBFile
extends GridFSFile {
    public InputStream getInputStream() {
        return new GridFSInputStream();
    }

    public long writeTo(String filename) throws IOException {
        return this.writeTo(new File(filename));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long writeTo(File file) throws IOException {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            long l = this.writeTo(out);
            return l;
        }
        finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public long writeTo(OutputStream out) throws IOException {
        int nc = this.numChunks();
        for (int i = 0; i < nc; ++i) {
            out.write(this.getChunk(i));
        }
        return this.length;
    }

    private byte[] getChunk(int chunkNumber) {
        if (this.fs == null) {
            throw new IllegalStateException("No GridFS instance defined!");
        }
        DBObject chunk = this.fs.getChunksCollection().findOne(new BasicDBObject("files_id", this.id).append("n", chunkNumber));
        if (chunk == null) {
            throw new MongoException("Can't find a chunk!  file id: " + this.id + " chunk: " + chunkNumber);
        }
        return (byte[])chunk.get("data");
    }

    void remove() {
        this.fs.getFilesCollection().remove(new BasicDBObject("_id", this.id));
        this.fs.getChunksCollection().remove(new BasicDBObject("files_id", this.id));
    }

    private class GridFSInputStream
    extends InputStream {
        private final int numberOfChunks;
        private int currentChunkId = -1;
        private int offset = 0;
        private byte[] buffer = null;

        GridFSInputStream() {
            this.numberOfChunks = GridFSDBFile.this.numChunks();
        }

        @Override
        public int available() {
            if (this.buffer == null) {
                return 0;
            }
            return this.buffer.length - this.offset;
        }

        @Override
        public int read() {
            byte[] b = new byte[1];
            int res = this.read(b);
            if (res < 0) {
                return -1;
            }
            return b[0] & 255;
        }

        @Override
        public int read(byte[] b) {
            return this.read(b, 0, b.length);
        }

        @Override
        public int read(byte[] b, int off, int len) {
            if (this.buffer == null || this.offset >= this.buffer.length) {
                if (this.currentChunkId + 1 >= this.numberOfChunks) {
                    return -1;
                }
                this.buffer = GridFSDBFile.this.getChunk(++this.currentChunkId);
                this.offset = 0;
            }
            int r = Math.min(len, this.buffer.length - this.offset);
            System.arraycopy(this.buffer, this.offset, b, off, r);
            this.offset += r;
            return r;
        }

        @Override
        public long skip(long bytesToSkip) throws IOException {
            if (bytesToSkip <= 0L) {
                return 0L;
            }
            if (this.currentChunkId == this.numberOfChunks) {
                return 0L;
            }
            long offsetInFile = 0L;
            if (this.currentChunkId >= 0) {
                offsetInFile = (long)this.currentChunkId * GridFSDBFile.this.chunkSize + (long)this.offset;
            }
            if (bytesToSkip + offsetInFile >= GridFSDBFile.this.length) {
                this.currentChunkId = this.numberOfChunks;
                this.buffer = null;
                return GridFSDBFile.this.length - offsetInFile;
            }
            int temp = this.currentChunkId;
            this.currentChunkId = (int)((bytesToSkip + offsetInFile) / GridFSDBFile.this.chunkSize);
            if (temp != this.currentChunkId) {
                this.buffer = GridFSDBFile.this.getChunk(this.currentChunkId);
            }
            this.offset = (int)((bytesToSkip + offsetInFile) % GridFSDBFile.this.chunkSize);
            return bytesToSkip;
        }
    }

}

