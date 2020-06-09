/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import org.bson.codecs.IdGenerator;
import org.bson.types.ObjectId;

public class ObjectIdGenerator
implements IdGenerator {
    @Override
    public Object generate() {
        return new ObjectId();
    }
}

