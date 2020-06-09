/*
 * Decompiled with CFR 0.145.
 */
package org.bson.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import org.bson.util.CopyOnWriteMap;

class ClassAncestry {
    private static final ConcurrentMap<Class<?>, List<Class<?>>> _ancestryCache = CopyOnWriteMap.newHashMap();

    ClassAncestry() {
    }

    public static <T> List<Class<?>> getAncestry(Class<T> c) {
        ConcurrentMap<Class<?>, List<Class<?>>> cache = ClassAncestry.getClassAncestryCache();
        List cachedResult;
        while ((cachedResult = (List)cache.get(c)) == null) {
            cache.putIfAbsent(c, ClassAncestry.computeAncestry(c));
        }
        return cachedResult;
    }

    private static List<Class<?>> computeAncestry(Class<?> c) {
        ArrayList result = new ArrayList();
        result.add(Object.class);
        ClassAncestry.computeAncestry(c, result);
        Collections.reverse(result);
        return Collections.unmodifiableList(new ArrayList(result));
    }

    private static <T> void computeAncestry(Class<T> c, List<Class<?>> result) {
        if (c == null || c == Object.class) {
            return;
        }
        Class<?>[] interfaces = c.getInterfaces();
        for (int i = interfaces.length - 1; i >= 0; --i) {
            ClassAncestry.computeAncestry(interfaces[i], result);
        }
        ClassAncestry.computeAncestry(c.getSuperclass(), result);
        if (!result.contains(c)) {
            result.add(c);
        }
    }

    private static ConcurrentMap<Class<?>, List<Class<?>>> getClassAncestryCache() {
        return _ancestryCache;
    }
}

