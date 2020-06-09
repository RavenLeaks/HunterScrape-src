/*
 * Decompiled with CFR 0.145.
 */
package org.bson.util;

import java.util.List;
import java.util.Map;
import org.bson.util.ClassAncestry;
import org.bson.util.ComputingMap;
import org.bson.util.CopyOnWriteMap;
import org.bson.util.Function;

@Deprecated
public class ClassMap<T> {
    private final Map<Class<?>, T> map = CopyOnWriteMap.newHashMap();
    private final Map<Class<?>, T> cache = ComputingMap.create(new ComputeFunction());

    public static <T> List<Class<?>> getAncestry(Class<T> clazz) {
        return ClassAncestry.getAncestry(clazz);
    }

    public T get(Object key) {
        return this.cache.get(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public T put(Class<?> key, T value) {
        try {
            T t = this.map.put(key, value);
            return t;
        }
        finally {
            this.cache.clear();
        }
    }

    public T remove(Object key) {
        try {
            T t = this.map.remove(key);
            return t;
        }
        finally {
            this.cache.clear();
        }
    }

    public void clear() {
        this.map.clear();
        this.cache.clear();
    }

    public int size() {
        return this.map.size();
    }

    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    private final class ComputeFunction
    implements Function<Class<?>, T> {
        private ComputeFunction() {
        }

        @Override
        public T apply(Class<?> a) {
            for (Class<?> cls : ClassMap.getAncestry(a)) {
                Object result = ClassMap.this.map.get(cls);
                if (result == null) continue;
                return (T)result;
            }
            return null;
        }
    }

}

