/*
 * Decompiled with CFR 0.145.
 */
package org.bson.codecs;

import org.bson.codecs.Decoder;
import org.bson.codecs.Encoder;

public interface Codec<T>
extends Encoder<T>,
Decoder<T> {
}

