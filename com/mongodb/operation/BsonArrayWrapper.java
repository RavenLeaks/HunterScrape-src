/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.operation;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.bson.BsonArray;
import org.bson.BsonValue;
import org.bson.assertions.Assertions;

class BsonArrayWrapper<T>
extends BsonArray {
    private final List<T> wrappedArray;

    BsonArrayWrapper(List<T> wrappedArray) {
        this.wrappedArray = Assertions.notNull("wrappedArray", wrappedArray);
    }

    public List<T> getWrappedArray() {
        return this.wrappedArray;
    }

    @Override
    public List<BsonValue> getValues() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<BsonValue> iterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(BsonValue bsonValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends BsonValue> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends BsonValue> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BsonValue get(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BsonValue set(int index, BsonValue element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, BsonValue element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BsonValue remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BsonValue> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<BsonValue> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<BsonValue> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        BsonArrayWrapper that = (BsonArrayWrapper)o;
        return this.wrappedArray.equals(that.wrappedArray);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.wrappedArray.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BsonArrayWrapper{wrappedArray=" + this.wrappedArray + '}';
    }

    @Override
    public BsonArray clone() {
        throw new UnsupportedOperationException("This should never be called on an instance of this type");
    }
}

