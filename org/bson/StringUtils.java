/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

import java.util.Collection;
import java.util.Iterator;

final class StringUtils {
    public static String join(String delimiter, Collection<?> s) {
        StringBuilder builder = new StringBuilder();
        Iterator<?> iter = s.iterator();
        while (iter.hasNext()) {
            builder.append(iter.next());
            if (!iter.hasNext()) break;
            builder.append(delimiter);
        }
        return builder.toString();
    }

    private StringUtils() {
    }
}

