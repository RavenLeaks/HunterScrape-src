/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.DBObject;
import com.mongodb.lang.Nullable;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.bson.BSONObject;

@Deprecated
public abstract class ReflectionDBObject
implements DBObject {
    JavaWrapper _wrapper;
    Object _id;
    private static final Map<Class, JavaWrapper> _wrappers = Collections.synchronizedMap(new HashMap());
    private static final Set<String> IGNORE_FIELDS = new HashSet<String>();

    @Nullable
    @Override
    public Object get(String key) {
        return this.getWrapper().get(this, key);
    }

    @Override
    public Set<String> keySet() {
        return this.getWrapper().keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return this.containsField(key);
    }

    @Override
    public boolean containsField(String fieldName) {
        return this.getWrapper().containsKey(fieldName);
    }

    @Override
    public Object put(String key, Object v) {
        return this.getWrapper().set(this, key, v);
    }

    @Override
    public void putAll(Map m) {
        for (Map.Entry entry : m.entrySet()) {
            this.put(entry.getKey().toString(), entry.getValue());
        }
    }

    @Override
    public void putAll(BSONObject o) {
        for (String k : o.keySet()) {
            this.put(k, o.get(k));
        }
    }

    public Object get_id() {
        return this._id;
    }

    public void set_id(Object id) {
        this._id = id;
    }

    @Override
    public boolean isPartialObject() {
        return false;
    }

    @Override
    public Map toMap() {
        HashMap<String, Object> m = new HashMap<String, Object>();
        for (String s : this.keySet()) {
            m.put(s, this.get(s + ""));
        }
        return m;
    }

    @Override
    public void markAsPartialObject() {
        throw new RuntimeException("ReflectionDBObjects can't be partial");
    }

    @Override
    public Object removeField(String key) {
        throw new UnsupportedOperationException("can't remove from a ReflectionDBObject");
    }

    JavaWrapper getWrapper() {
        if (this._wrapper != null) {
            return this._wrapper;
        }
        this._wrapper = ReflectionDBObject.getWrapper(this.getClass());
        return this._wrapper;
    }

    @Nullable
    public static JavaWrapper getWrapperIfReflectionObject(Class c) {
        if (ReflectionDBObject.class.isAssignableFrom(c)) {
            return ReflectionDBObject.getWrapper(c);
        }
        return null;
    }

    public static JavaWrapper getWrapper(Class c) {
        JavaWrapper w = _wrappers.get(c);
        if (w == null) {
            w = new JavaWrapper(c);
            _wrappers.put(c, w);
        }
        return w;
    }

    static {
        IGNORE_FIELDS.add("Int");
    }

    static class FieldInfo {
        final String name;
        final Class<? extends DBObject> clazz;
        Method getter;
        Method setter;

        FieldInfo(String name, Class<? extends DBObject> clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        boolean ok() {
            return this.getter != null && this.setter != null;
        }
    }

    public static class JavaWrapper {
        final Class clazz;
        final String name;
        final Map<String, FieldInfo> fields;
        final Set<String> keys;

        JavaWrapper(Class c) {
            this.clazz = c;
            this.name = c.getName();
            this.fields = new TreeMap<String, FieldInfo>();
            for (Method m : c.getMethods()) {
                String name;
                if (!m.getName().startsWith("get") && !m.getName().startsWith("set") || (name = m.getName().substring(3)).length() == 0 || IGNORE_FIELDS.contains(name)) continue;
                Class<?> type = m.getName().startsWith("get") ? m.getReturnType() : m.getParameterTypes()[0];
                FieldInfo fi = this.fields.get(name);
                if (fi == null) {
                    fi = new FieldInfo(name, type);
                    this.fields.put(name, fi);
                }
                if (m.getName().startsWith("get")) {
                    fi.getter = m;
                    continue;
                }
                fi.setter = m;
            }
            HashSet<String> names = new HashSet<String>(this.fields.keySet());
            for (String name : names) {
                if (this.fields.get(name).ok()) continue;
                this.fields.remove(name);
            }
            this.keys = Collections.unmodifiableSet(this.fields.keySet());
        }

        public Set<String> keySet() {
            return this.keys;
        }

        @Deprecated
        public boolean containsKey(String key) {
            return this.keys.contains(key);
        }

        @Nullable
        public Object get(ReflectionDBObject document, String fieldName) {
            FieldInfo i = this.fields.get(fieldName);
            if (i == null) {
                return null;
            }
            try {
                return i.getter.invoke(document, new Object[0]);
            }
            catch (Exception e) {
                throw new RuntimeException("could not invoke getter for [" + fieldName + "] on [" + this.name + "]", e);
            }
        }

        public Object set(ReflectionDBObject document, String fieldName, Object value) {
            FieldInfo i = this.fields.get(fieldName);
            if (i == null) {
                throw new IllegalArgumentException("no field [" + fieldName + "] on [" + this.name + "]");
            }
            try {
                return i.setter.invoke(document, value);
            }
            catch (Exception e) {
                throw new RuntimeException("could not invoke setter for [" + fieldName + "] on [" + this.name + "]", e);
            }
        }

        @Nullable
        Class<? extends DBObject> getInternalClass(List<String> path) {
            String cur = path.get(0);
            FieldInfo fi = this.fields.get(cur);
            if (fi == null) {
                return null;
            }
            if (path.size() == 1) {
                return fi.clazz;
            }
            JavaWrapper w = ReflectionDBObject.getWrapperIfReflectionObject(fi.clazz);
            if (w == null) {
                return null;
            }
            return w.getInternalClass(path.subList(1, path.size()));
        }
    }

}

