package org.example.whzbot.helper;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class MinHeap<T extends Comparable<T>> implements Collection<T> {
    T[] array;
    int cap;
    int len;

    public MinHeap(T[] a) {
        this.array = a.clone();
        this.cap = a.length;
        this.len = a.length;
        buildHeap();
    }

    private void buildHeap() {
        for (int i = (this.len - 1) / 2; i >= 0; i--) {
            sink(i);
        }
    }

    /*
     *  Put last element to front, re-heap and reduce len
     *  Return the first element.
     */
    public T extract() {
        T rtn = this.array[0];
        this.len--;
        this.array[0] = this.array[this.len];
        sink(0);
        return rtn;
    }

    public T top() {
        return this.array[0];
    }

    public T replaceTop(T new_top) {
        T rtn = this.array[0];
        this.array[0] = new_top;
        this.sink(0);
        return rtn;
    }

    /**
     * Swap a node with its parent node.
     * @param i index of child node.
     */
    private void swap(int i) {
        T temp = this.array[i];
        int j = (i - 1) / 2;
        this.array[i] = this.array[j];
        this.array[j] = temp;
    }

    /**
     * sink a node down to where it should be.
     * @param i index of start node.
     */
    private void sink(int i) {
        int j = i * 2 + 1;
        while (j < this.len) {
            if (this.array[i].compareTo(this.array[j]) > 0) {
                if (j + 1 < this.len &&
                        this.array[j].compareTo(this.array[j + 1]) > 0)
                    j++;
            } else if (j + 1 < this.len &&
                    this.array[i].compareTo(this.array[j + 1]) > 0)
                j++;
            else
                break;
            this.swap(j);
            i = j;
            j = i * 2 + 1;
        }
    }

    @Override
    public int size() {
        return this.len;
    }

    @Override
    public boolean isEmpty() {
        return this.len == 0;
    }

    @Override
    public boolean contains(Object o) {
        return Arrays.asList(this.array).contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return (Arrays.asList(this.array)).iterator();
    }

    @Override
    public Object[] toArray() {
        return this.array.clone();
    }

    @Override
    public <E> E[] toArray(E[] ts) {
        if (this.len == 0)
            return ts;
        if (ts.length < this.len)
            return (E[]) this.array.clone();
        for (int i = 0; i < len; i++)
            ts[i] = (E) this.array[i];
        return ts;
    }

    @Override
    public boolean add(T t) {
        if (this.len == this.cap)
            return false;
        this.array[len] = t;
        int i = this.len;
        this.len++;
        int j = (i - 1) / 2;
        while (j > 0 && this.array[i].compareTo(this.array[j]) < 0) {
            this.swap(i);
            j = i;
            i = (i - 1) / 2;
        }

        return true;
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return Arrays.asList(this.array).containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        if (collection.size() + len > cap)
            return false;
        for (T t : collection)
            this.add(t);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {
        this.len = 0;
        this.array = null;
    }
}
