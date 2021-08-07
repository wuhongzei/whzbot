package org.example.whzbot.storage.json;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class JsonListNode extends JsonNode implements List<JsonNode> {
    protected LinkedList<JsonNode> content;

    public JsonListNode() {
        this("");
    }

    public JsonListNode(String name) {
        super(name);
        this.content = new LinkedList<>();
    }

    @Override
    public int size() {
        return this.content.size();
    }

    public boolean isEmpty() {
        return this.content.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.content.contains(o);
    }

    @Override
    public Iterator<JsonNode> iterator() {
        return this.content.iterator();
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return null;
    }

    public boolean add(JsonNode item) {
        return this.content.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(Collection<? extends JsonNode> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, Collection<? extends JsonNode> collection) {
        return false;
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
        this.content.clear();
    }

    @Override
    public JsonNode get(int i) {
        return this.content.get(i);
    }

    @Override
    public JsonNode set(int i, JsonNode jsonNode) {
        return null;
    }

    @Override
    public void add(int i, JsonNode jsonNode) {

    }

    @Override
    public JsonNode remove(int i) {
        return null;
    }

    @Override
    public int indexOf(Object o) {
        return this.content.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return this.content.lastIndexOf(o);
    }

    @Override
    public ListIterator<JsonNode> listIterator() {
        return this.content.listIterator();
    }

    @Override
    public ListIterator<JsonNode> listIterator(int i) {
        return this.content.listIterator(i);
    }

    @Override
    public List<JsonNode> subList(int i, int j) {
        return this.content.subList(i, j);
    }

    public JsonNode get(String path) {
        if (path.isBlank())
            return this;
        int end_of_int = path.indexOf(']');
        String index_str = path.substring(0, end_of_int);
        try {
            int index = Integer.parseInt(index_str);
            if (index < 0 || index >= this.content.size()) {
                new IndexOutOfBoundsException(index).printStackTrace();
                return null;
            }
            end_of_int = path.indexOf('.', end_of_int);
            if (end_of_int == -1)
                return null;
            else if (end_of_int + 1 < path.length()) {
                return this.content.get(index).get(
                        path.substring(path.indexOf(end_of_int + 1))
                );
            }
            else {
                return this.content.get(index).get("");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getContent() {
        StringBuilder builder = new StringBuilder("[");
        int i = this.content.size();
        for (JsonNode item : this.content) {
            if (item instanceof JsonStringNode)
                builder.append("\"").
                        append(item.getContent()).
                        append("\"");
            else
                builder.append(item.getContent());
            i--;
            if (i != 0)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(String.format("\"%s\":[", this.name));
        int i = this.content.size();
        for (JsonNode item : this.content) {
            builder.append(item.getContent());
            i--;
            if (i != 0)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }
}
