/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBEncoder;
import com.mongodb.LazyDBObject;
import com.mongodb.MongoException;
import java.io.IOException;
import java.io.OutputStream;
import org.bson.BSONObject;
import org.bson.io.OutputBuffer;

public class LazyDBEncoder
implements DBEncoder {
    @Override
    public int writeObject(OutputBuffer outputBuffer, BSONObject document) {
        if (!(document instanceof LazyDBObject)) {
            throw new IllegalArgumentException("LazyDBEncoder can only encode BSONObject instances of type LazyDBObject");
        }
        LazyDBObject lazyDBObject = (LazyDBObject)document;
        try {
            return lazyDBObject.pipe(outputBuffer);
        }
        catch (IOException e) {
            throw new MongoException("Exception serializing a LazyDBObject", e);
        }
    }
}

