/*
 * Decompiled with CFR 0.145.
 */
package org.bson.json;

import org.bson.json.Converter;
import org.bson.json.StrictJsonWriter;

class JsonBooleanConverter
implements Converter<Boolean> {
    JsonBooleanConverter() {
    }

    @Override
    public void convert(Boolean value, StrictJsonWriter writer) {
        writer.writeBoolean(value);
    }
}

