package org.example.whzbot.helper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class AreaHelper implements Iterable<Integer> {
    int width;
    int height;
    ArrayDeque<Integer> queue;

    public AreaHelper(int w, int h) {
        this.queue = new ArrayDeque<>();
        this.width = w;
        this.height = h;
    }

    /**
     * Empty selected area, prepare for reuse.
     */
    public void clear() {
        this.queue.clear();
    }

    //public int size() {return this.width * this.height;}
    public int size() {
        return this.queue.size();
    }

    /**
     * Add a location to selected area
     *
     * @param p a valid location, p=x*y, x in [0,w) and y in [0,h)
     */
    protected void add(int p) {
        this.queue.add(p);
    }

    /**
     * Add adjacent (up/down/left/right) location from centre.
     * Outbound edges ignored.
     *
     * @param centre centre location, select adjacent.
     * @return this
     */
    public AreaHelper addAdjacent(int centre) {
        int x = centre % this.width;
        int y = centre / this.width;
        if (x > 0)
            this.queue.add(centre - 1);
        if (x < this.width - 1)
            this.queue.add(centre + 1);
        if (y > 0)
            this.queue.add(centre - this.width);
        if (y < this.height - 1)
            this.queue.add(centre + this.width);
        return this;
    }

    /**
     * Select four sized edges centred. Outbound edges ignored.
     *
     * @param centre centre of square.
     * @param size   edge length of square.
     * @return this
     */
    public AreaHelper addSqrEdge(int centre, int size) {
        return this.addRectEdge(
                centre % this.width - size, centre / this.width - size,
                centre % this.width + size, centre / this.width + size
        );
    }

    /**
     * Select four edges (x1,y1), (x1,x2), (x2,y2), (y1,y2). Outbound area ignored.
     *
     * @param x1 first x index. Order insensitive, outbound save.
     * @param y1 first y index.
     * @param x2 second x index.
     * @param y2 second y index.
     * @return this
     */
    public AreaHelper addRectEdge(int x1, int y1, int x2, int y2) {
        int a, b, c, d;
        a = x1;
        b = y1;
        c = x2;
        d = y2;

        // (a, b), (c, d) and 0 < a < c < w, 0 < b < d < h
        int dif = c - a;
        a = a + dif * (dif >> 31 & 1);
        c = c - dif * (dif >> 31 & 1);
        dif = d - b;
        b = b + dif * (dif >> 31 & 1);
        d = d - dif * (dif >> 31 & 1);

        dif = this.width - c - 1;
        int i1 = a - a * (a >> 31 & 1);
        int i2 = c + 1 + dif * (dif >> 31 & 1);
        if (b >= 0)
            for (int i = i1; i < i2; i++) {
                this.add(i + b * this.width);
            }
        if (d < this.height)
            for (int i = i1; i < i2; i++) {
                this.add(i + d * this.width);
            }

        b++;
        b = b - b * (b >> 31 & 1);
        dif = this.height - d;
        d = d + dif * (dif >> 31 & 1);
        if (a >= 0)
            for (int i = b; i < d; i++) {
                this.add(i * this.width + a);
            }
        if (c < this.height)
            for (int i = b; i < d; i++) {
                this.add(i * this.width + c);
            }
        return this;
    }

    /**
     * Select from a location, and add all connected valid area.
     * Connected: surrounding 8 locations should be valid.
     *
     * @param beg      beginning location.
     * @param is_valid Predicate to determine whether a new loc valid.
     * @param no_break Predicate to determine when should break happen, if breaks, search region may not stable.
     * @return this; if break occurs, return null.
     */
    public AreaHelper addRadiant(int beg, Predicate<Integer> is_valid, Predicate<Integer> no_break) {
        if (!is_valid.test(beg))
            return this;
        ArrayList<Integer> al = new ArrayList<>();
        ArrayList<Integer> ar = new ArrayList<>();
        ArrayList<Integer> au = new ArrayList<>();
        ArrayList<Integer> ad = new ArrayList<>();
        Set<Integer> query = new HashSet<>();
        Set<Integer> contain = new HashSet<>();
        boolean flag = true;
        int i;
        query.add(beg);
        contain.add(beg);
        if (this.hasDown(beg)) {
            if (this.hasLeft(beg)) {
                i = this.getDownLeft(beg);
                query.add(i);
                flag = no_break.test(i);
                if (is_valid.test(i))
                    ad.add(i);
            }
            i = this.getDown(beg);
            query.add(i);
            flag &= no_break.test(i);
            if (is_valid.test(i))
                ad.add(i);
            else
                ad.add(-1);
            if (this.hasRight(beg) && flag) {
                i = this.getDownRight(beg);
                query.add(i);
                flag = no_break.test(i);
                if (is_valid.test(i))
                    ad.add(i);
            }
            ad.add(-1);
        }
        if (this.hasUp(beg) && flag) {
            if (this.hasLeft(beg)) {
                i = this.getUpLeft(beg);
                query.add(i);
                flag = no_break.test(i);
                if (is_valid.test(i))
                    au.add(i);
            }
            i = this.getUp(beg);
            query.add(i);
            flag &= no_break.test(i);
            if (is_valid.test(i))
                au.add(i);
            else
                au.add(-1);
            if (this.hasRight(beg) && flag) {
                i = this.getUpRight(beg);
                query.add(i);
                flag = no_break.test(i);
                if (is_valid.test(i))
                    au.add(i);
            }
            au.add(-1);
        }
        if (this.hasLeft(beg) && flag) {
            i = this.getLeft(beg);
            query.add(i);
            flag = no_break.test(i);
            if (is_valid.test(i))
                al.add(i);
            al.add(-1);
        }
        if (this.hasRight(beg) && flag) {
            i = this.getRight(beg);
            query.add(i);
            flag = no_break.test(i);
            if (is_valid.test(i))
                ar.add(i);
            ar.add(-1);
        }
        if (!flag)
            return null;

        while (al.size() > 1 || ar.size() > 1 || au.size() > 1 || ad.size() > 1) {
            if (!testAll(no_break, ad))
                return null;
            contain.addAll(ad);
            ad = appendAndUpdate(
                    is_valid, ad, al, ar, this::hasDown, this::getDown,
                    this::hasLeft, this::hasRight, this::hasUp,
                    this::getLeft, this::getRight, this::getDownLeft,
                    this::getDownRight, this::getUpLeft, this::getUpRight,
                    query
            );
            if (!testAll(no_break, au))
                return null;
            contain.addAll(au);
            au = appendAndUpdate(
                    is_valid, au, al, ar, this::hasUp, this::getUp,
                    this::hasLeft, this::hasRight, this::hasDown,
                    this::getLeft, this::getRight, this::getUpLeft,
                    this::getUpRight, this::getDownLeft, this::getDownRight,
                    query
            );
            if (!testAll(no_break, al))
                return null;
            maintainAscend(al);
            maintainAscend(ar);
            contain.addAll(al);
            al = appendAndUpdate(
                    is_valid, al, ad, au, this::hasLeft, this::getLeft,
                    this::hasDown, this::hasUp, this::hasRight,
                    this::getDown, this::getUp, this::getDownLeft,
                    this::getUpLeft, this::getDownRight, this::getUpRight,
                    query
            );
            if (!testAll(no_break, ar))
                return null;
            contain.addAll(ar);
            ar = appendAndUpdate(
                    is_valid, ar, ad, au, this::hasRight, this::getRight,
                    this::hasDown, this::hasUp, this::hasLeft,
                    this::getDown, this::getUp, this::getDownRight,
                    this::getUpRight, this::getDownLeft, this::getUpLeft,
                    query
            );
            maintainAscend(au);
            maintainAscend(ad);
        }

        contain.remove(-1);
        this.queue.addAll(contain);
        return this;
    }

    /**
     * Helper method for AddRadiant. Check duplicate using a set, if not, add it.
     *
     * @param predicate check if valid.
     * @param array     array to put selected area temporarily
     * @param query     set to store selected locations.
     * @param i         new location value.
     */
    private void appendIfNew(Predicate<Integer> predicate, ArrayList<Integer> array, Set<Integer> query, int i) {
        if (!query.contains(i)) {
            if (predicate.test(i)) {
                array.add(i);
            }
            query.add(i);
        }
    }

    /**
     * Helper function for AddRadiant. From a list of known values, expand selected area.
     * Main direction x (left/right) / y (up/down)
     * direction_1 x: up / y: left
     * direction_2 x: down / y: right
     * direction_o: opposite direction.
     * array: ascending arrays separated by value -1. No beginning -1. duplicate -1 allowed.
     *
     * @param predicate  predicate to check if new location valid.
     * @param array      main array to expand. Array for main direction.
     * @param array_1    array for left/up direction. Will be appended.
     * @param array_2    array for right/down direction. Will be appended.
     * @param has_dir    check if possible to expand from main direction.
     * @param get_dir    get location to expand to main direction.
     * @param has_dir_1  check if possible to expand to direction_1.
     * @param has_dir_2  check if possible to expand to direction_2.
     * @param has_dir_o  check if possible to expand to opposite dir.
     * @param get_dir_1  get location expanding to dir_1
     * @param get_dir_2  get location expanding to dir_2
     * @param get_dir_d1 get location expanding to main dir + dir_1
     * @param get_dir_d2 get location expanding to main dir + dir_2
     * @param get_dir_o1 get location expanding to opposite dir + dir_1
     * @param get_dir_o2 get location expanding to opposite dir + dir_2
     * @param query      set to store queried locations.
     * @return A new array for main direction.
     */
    private ArrayList<Integer> appendAndUpdate(
            Predicate<Integer> predicate, ArrayList<Integer> array,
            ArrayList<Integer> array_1, ArrayList<Integer> array_2,
            Predicate<Integer> has_dir, Function<Integer, Integer> get_dir,
            Predicate<Integer> has_dir_1, Predicate<Integer> has_dir_2,
            Predicate<Integer> has_dir_o,
            Function<Integer, Integer> get_dir_1,
            Function<Integer, Integer> get_dir_2,
            Function<Integer, Integer> get_dir_d1,
            Function<Integer, Integer> get_dir_d2,
            Function<Integer, Integer> get_dir_o1,
            Function<Integer, Integer> get_dir_o2,
            Set<Integer> query) {
        ArrayList<Integer> temp = new ArrayList<>();
        if (array.size() > 1) {
            int index = 0;
            while (index < array.size() && array.get(index) == -1)
                index++;
            if (index > array.size() - 2)
                return temp;
            int i, j;
            while (index < array.size()) {
                j = array.get(index);
                if (has_dir_1.test(j)) {
                    i = get_dir_1.apply(j);
                    appendIfNew(predicate, array_1, query, i);
                    if (has_dir_o.test(j)) {
                        i = get_dir_o1.apply(j);
                        appendIfNew(predicate, array_1, query, i);
                    }
                    array_1.add(-1);
                    if (has_dir.test(j)) {
                        i = get_dir_d1.apply(j);
                        appendIfNew(predicate, temp, query, i);
                    }
                }
                if (has_dir.test(j)) {
                    while (array.get(index) != -1) {
                        j = array.get(index);
                        i = get_dir.apply(j);
                        if (!query.contains(i)) {
                            if (predicate.test(i)) {
                                temp.add(i);
                            } else {
                                temp.add(-1);
                            }
                            query.add(i);
                        }
                        index++;
                    }
                } else {
                    while (array.get(index) != -1)
                        index++;
                }
                j = array.get(index - 1);
                if (has_dir_2.test(j)) {
                    if (has_dir.test(j)) {
                        i = get_dir_d2.apply(j);
                        appendIfNew(predicate, temp, query, i);
                    }
                    i = get_dir_2.apply(j);
                    appendIfNew(predicate, array_2, query, i);
                    if (has_dir_o.test(j)) {
                        i = get_dir_o2.apply(j);
                        appendIfNew(predicate, array_2, query, i);
                    }
                    array_2.add(-1);
                }
                if (temp.size() > 0 && temp.get(temp.size() - 1) != -1)
                    temp.add(-1);
                while (index < array.size() && array.get(index) == -1)
                    index++;
            }
        }
        array.clear();
        return temp;
    }

    @NotNull
    @Override
    public Iterator<Integer> iterator() {
        return new AreaIterator(this.queue.clone());
    }

    public static class AreaIterator implements Iterator<Integer> {
        Queue<Integer> queue;

        protected AreaIterator(Queue<Integer> q) {
            this.queue = q;
        }

        @Override
        public boolean hasNext() {
            return !this.queue.isEmpty();
        }

        @Override
        public Integer next() {
            return this.queue.poll();
        }
    }


    protected boolean hasLeft(int i) {
        return i % this.width != 0;
    }

    protected boolean hasRight(int i) {
        return (i + 1) % this.width != 0;
    }

    protected boolean hasDown(int i) {
        return i / this.width > 0;
    }

    protected boolean hasUp(int i) {
        return i / this.width < this.height - 1;
    }

    protected int getLeft(int i) {
        return i - 1;
    }

    protected int getRight(int i) {
        return i + 1;
    }

    protected int getUp(int i) {
        return i + this.width;
    }

    protected int getDown(int i) {
        return i - this.width;
    }

    protected int getUpLeft(int i) {
        return i + this.width - 1;
    }

    protected int getDownLeft(int i) {
        return i - this.width - 1;
    }

    protected int getUpRight(int i) {
        return i + this.width + 1;
    }

    protected int getDownRight(int i) {
        return i - this.width + 1;
    }

    public static boolean testAll(Predicate<Integer> predicate, ArrayList<Integer> array) {
        boolean rtn = true;
        for (int i = 0; i < array.size(); i++) {
            rtn &= array.get(i) < 0 || predicate.test(array.get(i));
        }
        return rtn;
    }

    /**
     * Helper function to maintain -1 selected compound array ascending.
     *
     * @param array array needed to be sorted.
     */
    private static void maintainAscend(ArrayList<Integer> array) {
        int index = 0;
        while (index < array.size()) {
            if (array.get(index) != -1 && index < array.size() - 2
                    && array.get(index + 2) == -1) {
                int temp = array.get(index + 1);
                if (temp != -1 && array.get(index) > temp) {
                    array.set(index + 1, array.get(index));
                    array.set(index, temp);
                    index += 3;
                } else
                    index++;
            } else
                index++;
        }
    }
}
