/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DBObjectFactory;
import com.mongodb.MongoInternalException;
import com.mongodb.ReflectionDBObject;
import com.mongodb.annotations.Immutable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Immutable
final class DBCollectionObjectFactory
implements DBObjectFactory {
    private final Map<List<String>, Class<? extends DBObject>> pathToClassMap;
    private final ReflectionDBObject.JavaWrapper wrapper;

    DBCollectionObjectFactory() {
        this(Collections.emptyMap(), null);
    }

    private DBCollectionObjectFactory(Map<List<String>, Class<? extends DBObject>> pathToClassMap, ReflectionDBObject.JavaWrapper wrapper) {
        this.pathToClassMap = pathToClassMap;
        this.wrapper = wrapper;
    }

    @Override
    public DBObject getInstance() {
        return this.getInstance(Collections.<String>emptyList());
    }

    @Override
    public DBObject getInstance(List<String> path) {
        Class<? extends DBObject> aClass = this.getClassForPath(path);
        try {
            return aClass.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
        }
        catch (InstantiationException e) {
            throw this.createInternalException(aClass, e);
        }
        catch (IllegalAccessException e) {
            throw this.createInternalException(aClass, e);
        }
        catch (NoSuchMethodException e) {
            throw this.createInternalException(aClass, e);
        }
        catch (InvocationTargetException e) {
            throw this.createInternalException(aClass, e.getTargetException());
        }
    }

    public DBCollectionObjectFactory update(Class<? extends DBObject> aClass) {
        return new DBCollectionObjectFactory(this.updatePathToClassMap(aClass, Collections.<String>emptyList()), this.isReflectionDBObject(aClass) ? ReflectionDBObject.getWrapper(aClass) : this.wrapper);
    }

    public DBCollectionObjectFactory update(Class<? extends DBObject> aClass, List<String> path) {
        return new DBCollectionObjectFactory(this.updatePathToClassMap(aClass, path), this.wrapper);
    }

    private Map<List<String>, Class<? extends DBObject>> updatePathToClassMap(Class<? extends DBObject> aClass, List<String> path) {
        HashMap<List<String>, Class<? extends DBObject>> map = new HashMap<List<String>, Class<? extends DBObject>>(this.pathToClassMap);
        if (aClass != null) {
            map.put(path, aClass);
        } else {
            map.remove(path);
        }
        return map;
    }

    Class<? extends DBObject> getClassForPath(List<String> path) {
        if (this.pathToClassMap.containsKey(path)) {
            return this.pathToClassMap.get(path);
        }
        Class<? extends DBObject> aClass = this.wrapper != null ? this.wrapper.getInternalClass(path) : null;
        return aClass != null ? aClass : BasicDBObject.class;
    }

    private boolean isReflectionDBObject(Class<? extends DBObject> aClass) {
        return aClass != null && ReflectionDBObject.class.isAssignableFrom(aClass);
    }

    private MongoInternalException createInternalException(Class<? extends DBObject> aClass, Throwable e) {
        throw new MongoInternalException("Can't instantiate class " + aClass, e);
    }
}

