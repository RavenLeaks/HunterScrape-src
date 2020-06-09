/*
 * Decompiled with CFR 0.145.
 */
package org.bson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonValue;
import org.bson.codecs.BsonArrayCodec;
import org.bson.codecs.DecoderContext;
import org.bson.json.JsonReader;

public class BsonArray
extends BsonValue
implements List<BsonValue>,
Cloneable {
    private final List<BsonValue> values;

    public BsonArray(List<? extends BsonValue> values) {
        this(values, true);
    }

    public BsonArray() {
        this(new ArrayList(), false);
    }

    BsonArray(List<? extends BsonValue> values, boolean copy) {
        this.values = copy ? new ArrayList<BsonValue>(values) : values;
    }

    public static BsonArray parse(String json) {
        return new BsonArrayCodec().decode(new JsonReader(json), DecoderContext.builder().build());
    }

    public List<BsonValue> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    @Override
    public BsonType getBsonType() {
        return BsonType.ARRAY;
    }

    @Override
    public int size() {
        return this.values.size();
    }

    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.values.contains(o);
    }

    @Override
    public Iterator<BsonValue> iterator() {
        return this.values.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.values.toArray(a);
    }

    @Override
    public boolean add(BsonValue bsonValue) {
        return this.values.add(bsonValue);
    }

    @Override
    public boolean remove(Object o) {
        return this.values.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.values.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends BsonValue> c) {
        return this.values.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends BsonValue> c) {
        return this.values.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.values.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.values.retainAll(c);
    }

    @Override
    public void clear() {
        this.values.clear();
    }

    @Override
    public BsonValue get(int index) {
        return this.values.get(index);
    }

    @Override
    public BsonValue set(int index, BsonValue element) {
        return this.values.set(index, element);
    }

    @Override
    public void add(int index, BsonValue element) {
        this.values.add(index, element);
    }

    @Override
    public BsonValue remove(int index) {
        return this.values.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return this.values.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.values.lastIndexOf(o);
    }

    @Override
    public ListIterator<BsonValue> listIterator() {
        return this.values.listIterator();
    }

    @Override
    public ListIterator<BsonValue> listIterator(int index) {
        return this.values.listIterator(index);
    }

    @Override
    public List<BsonValue> subList(int fromIndex, int toIndex) {
        return this.values.subList(fromIndex, toIndex);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BsonArray)) {
            return false;
        }
        BsonArray that = (BsonArray)o;
        return this.getValues().equals(that.getValues());
    }

    @Override
    public int hashCode() {
        return this.values.hashCode();
    }

    public String toString() {
        return "BsonArray{values=" + this.values + '}';
    }

    public BsonArray clone() {
        BsonArray to = new BsonArray();
        block6 : for (BsonValue cur : this) {
            switch (cur.getBsonType()) {
                case DOCUMENT: {
                    to.add(cur.asDocument().clone());
                    continue block6;
                }
                case ARRAY: {
                    to.add(cur.asArray().clone());
                    continue block6;
                }
                case BINARY: {
                    to.add(BsonBinary.clone(cur.asBinary()));
                    continue block6;
                }
                case JAVASCRIPT_WITH_SCOPE: {
                    to.add(BsonJavaScriptWithScope.clone(cur.asJavaScriptWithScope()));
                    continue block6;
                }
            }
            to.add(cur);
        }
        return to;
    }

}

