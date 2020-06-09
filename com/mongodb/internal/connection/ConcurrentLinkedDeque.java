/*
 * Decompiled with CFR 0.145.
 */
package com.mongodb.internal.connection;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

public class ConcurrentLinkedDeque<E>
extends AbstractCollection<E>
implements Deque<E>,
Serializable {
    private static final long serialVersionUID = 876323262645176354L;
    private final Node<E> header;
    private final Node<E> trailer;

    private static boolean usable(Node<?> n) {
        return n != null && !n.isSpecial();
    }

    private static void checkNotNull(Object v) {
        if (v == null) {
            throw new NullPointerException();
        }
    }

    private E screenNullResult(E v) {
        if (v == null) {
            throw new NoSuchElementException();
        }
        return v;
    }

    private ArrayList<E> toArrayList() {
        ArrayList c = new ArrayList();
        for (Node<E> n = this.header.forward(); n != null; n = n.forward()) {
            c.add(n.element);
        }
        return c;
    }

    public ConcurrentLinkedDeque() {
        Node<Object> h = new Node<Object>(null, null, null);
        Node<Object> t = new Node<Object>(null, null, h);
        h.setNext(t);
        this.header = h;
        this.trailer = t;
    }

    public ConcurrentLinkedDeque(Collection<? extends E> c) {
        this();
        this.addAll(c);
    }

    @Override
    public void addFirst(E e) {
        ConcurrentLinkedDeque.checkNotNull(e);
        while (this.header.append(e) == null) {
        }
    }

    @Override
    public void addLast(E e) {
        ConcurrentLinkedDeque.checkNotNull(e);
        while (this.trailer.prepend(e) == null) {
        }
    }

    @Override
    public boolean offerFirst(E e) {
        this.addFirst(e);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        this.addLast(e);
        return true;
    }

    @Override
    public E peekFirst() {
        Node<E> n = this.header.successor();
        return n == null ? null : (E)n.element;
    }

    @Override
    public E peekLast() {
        Node<E> n = this.trailer.predecessor();
        return n == null ? null : (E)n.element;
    }

    @Override
    public E getFirst() {
        return this.screenNullResult(this.peekFirst());
    }

    @Override
    public E getLast() {
        return this.screenNullResult(this.peekLast());
    }

    @Override
    public E pollFirst() {
        Node<E> n;
        do {
            if (ConcurrentLinkedDeque.usable(n = this.header.successor())) continue;
            return null;
        } while (!n.delete());
        return n.element;
    }

    @Override
    public E pollLast() {
        Node<E> n;
        do {
            if (ConcurrentLinkedDeque.usable(n = this.trailer.predecessor())) continue;
            return null;
        } while (!n.delete());
        return n.element;
    }

    @Override
    public E removeFirst() {
        return this.screenNullResult(this.pollFirst());
    }

    @Override
    public E removeLast() {
        return this.screenNullResult(this.pollLast());
    }

    @Override
    public boolean offer(E e) {
        return this.offerLast(e);
    }

    @Override
    public boolean add(E e) {
        return this.offerLast(e);
    }

    @Override
    public E poll() {
        return this.pollFirst();
    }

    @Override
    public E remove() {
        return this.removeFirst();
    }

    @Override
    public E peek() {
        return this.peekFirst();
    }

    @Override
    public E element() {
        return this.getFirst();
    }

    @Override
    public void push(E e) {
        this.addFirst(e);
    }

    @Override
    public E pop() {
        return this.removeFirst();
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        ConcurrentLinkedDeque.checkNotNull(o);
        block0 : do {
            Node<E> n = this.header.forward();
            do {
                if (n == null) {
                    return false;
                }
                if (o.equals(n.element)) {
                    if (!n.delete()) continue block0;
                    return true;
                }
                n = n.forward();
            } while (true);
            break;
        } while (true);
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    @Override
    public boolean removeLastOccurrence(Object o) {
        ConcurrentLinkedDeque.checkNotNull(o);
        block0 : do lbl-1000: // 3 sources:
        {
            s = this.trailer;
            do {
                n = s.back();
                if (s.isDeleted() || n != null && n.successor() != s) ** GOTO lbl-1000
                if (n == null) {
                    return false;
                }
                if (o.equals(n.element)) {
                    if (!n.delete()) continue block0;
                    return true;
                }
                s = n;
            } while (true);
            break;
        } while (true);
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            return false;
        }
        for (Node<E> n = this.header.forward(); n != null; n = n.forward()) {
            if (!o.equals(n.element)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return !ConcurrentLinkedDeque.usable(this.header.successor());
    }

    @Override
    public int size() {
        long count = 0L;
        for (Node<E> n = this.header.forward(); n != null; n = n.forward()) {
            ++count;
        }
        return count >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)count;
    }

    @Override
    public boolean remove(Object o) {
        return this.removeFirstOccurrence(o);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Iterator<E> it = c.iterator();
        if (!it.hasNext()) {
            return false;
        }
        do {
            this.addLast(it.next());
        } while (it.hasNext());
        return true;
    }

    @Override
    public void clear() {
        while (this.pollFirst() != null) {
        }
    }

    @Override
    public Object[] toArray() {
        return this.toArrayList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.toArrayList().toArray(a);
    }

    @Override
    public RemovalReportingIterator<E> iterator() {
        return new CLDIterator();
    }

    @Override
    public Iterator<E> descendingIterator() {
        throw new UnsupportedOperationException();
    }

    public static interface RemovalReportingIterator<E>
    extends Iterator<E> {
        public boolean reportingRemove();
    }

    final class CLDIterator
    implements RemovalReportingIterator<E> {
        Node<E> last;
        Node<E> next;

        CLDIterator() {
            this.next = ConcurrentLinkedDeque.this.header.forward();
        }

        @Override
        public boolean hasNext() {
            return this.next != null;
        }

        @Override
        public E next() {
            this.last = this.next;
            Node<E> l = this.last;
            if (l == null) {
                throw new NoSuchElementException();
            }
            this.next = this.next.forward();
            return l.element;
        }

        @Override
        public void remove() {
            this.reportingRemove();
        }

        @Override
        public boolean reportingRemove() {
            Node<E> l = this.last;
            if (l == null) {
                throw new IllegalStateException();
            }
            boolean successfullyRemoved = l.delete();
            while (!successfullyRemoved && !l.isDeleted()) {
                successfullyRemoved = l.delete();
            }
            return successfullyRemoved;
        }
    }

    static final class Node<E>
    extends AtomicReference<Node<E>> {
        private volatile Node<E> prev;
        final E element;
        private static final long serialVersionUID = 876323262645176354L;

        Node(E element, Node<E> next, Node<E> prev) {
            super(next);
            this.prev = prev;
            this.element = element;
        }

        Node(Node<E> next) {
            super(next);
            this.prev = this;
            this.element = null;
        }

        private Node<E> getNext() {
            return (Node)this.get();
        }

        void setNext(Node<E> n) {
            this.set(n);
        }

        private boolean casNext(Node<E> cmp, Node<E> val) {
            return this.compareAndSet(cmp, val);
        }

        private Node<E> getPrev() {
            return this.prev;
        }

        void setPrev(Node<E> b) {
            this.prev = b;
        }

        boolean isSpecial() {
            return this.element == null;
        }

        boolean isTrailer() {
            return this.getNext() == null;
        }

        boolean isHeader() {
            return this.getPrev() == null;
        }

        boolean isMarker() {
            return this.getPrev() == this;
        }

        boolean isDeleted() {
            Node<E> f = this.getNext();
            return f != null && f.isMarker();
        }

        private Node<E> nextNonmarker() {
            Node<E> f = this.getNext();
            return f == null || !f.isMarker() ? f : Node.super.getNext();
        }

        Node<E> successor() {
            Node<E> f = this.nextNonmarker();
            while (f != null) {
                if (!f.isDeleted()) {
                    if (Node.super.getPrev() != this && !this.isDeleted()) {
                        f.setPrev(this);
                    }
                    return f;
                }
                Node<E> s = Node.super.nextNonmarker();
                if (f == this.getNext()) {
                    this.casNext(f, s);
                }
                f = s;
            }
            return null;
        }

        private Node<E> findPredecessorOf(Node<E> target) {
            Node<E> n = this;
            Node<E> f;
            while ((f = n.successor()) != target) {
                if (f == null) {
                    return null;
                }
                n = f;
            }
            return n;
        }

        Node<E> predecessor() {
            Node<E> n = this;
            Node<E> b;
            while ((b = n.getPrev()) != null) {
                Node<E> p;
                Node<E> s = Node.super.getNext();
                if (s == this) {
                    return b;
                }
                if (!(s != null && s.isMarker() || (p = Node.super.findPredecessorOf(this)) == null)) {
                    return p;
                }
                n = b;
            }
            return n.findPredecessorOf(this);
        }

        Node<E> forward() {
            Node<E> f = this.successor();
            return f == null || f.isSpecial() ? null : f;
        }

        Node<E> back() {
            Node<E> f = this.predecessor();
            return f == null || f.isSpecial() ? null : f;
        }

        Node<E> append(E element) {
            Node<E> x;
            Node<E> f;
            do {
                if ((f = this.getNext()) != null && !f.isMarker()) continue;
                return null;
            } while (!this.casNext(f, x = new Node<E>(element, f, this)));
            f.setPrev(x);
            return x;
        }

        Node<E> prepend(E element) {
            Node<E> x;
            Node<E> b;
            do {
                if ((b = this.predecessor()) != null) continue;
                return null;
            } while (!Node.super.casNext(this, x = new Node<E>(element, this, b)));
            this.setPrev(x);
            return x;
        }

        boolean delete() {
            Node<E> b = this.getPrev();
            Node<E> f = this.getNext();
            if (b != null && f != null && !f.isMarker() && this.casNext(f, new Node<E>(f))) {
                if (Node.super.casNext(this, f)) {
                    f.setPrev(b);
                }
                return true;
            }
            return false;
        }

        Node<E> replace(E newElement) {
            Node<E> f;
            Node<E> x;
            Node<E> b;
            do {
                b = this.getPrev();
                f = this.getNext();
                if (b != null && f != null && !f.isMarker()) continue;
                return null;
            } while (!this.casNext(f, new Node<E>(x = new Node<E>(newElement, f, b))));
            b.successor();
            x.successor();
            return x;
        }
    }

}

