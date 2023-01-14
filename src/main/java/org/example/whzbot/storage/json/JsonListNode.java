package org.example.whzbot.storage.json;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Function;

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

    @NotNull
    @Override
    public Iterator<JsonNode> iterator() {
        return this.content.iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return ts;
    }

    public boolean add(JsonNode item) {
        return this.content.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends JsonNode> collection) {
        return false;
    }

    @Override
    public boolean addAll(int i, @NotNull Collection<? extends JsonNode> collection) {
        return false;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
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

    @NotNull
    @Override
    public ListIterator<JsonNode> listIterator() {
        return this.content.listIterator();
    }

    @NotNull
    @Override
    public ListIterator<JsonNode> listIterator(int i) {
        return this.content.listIterator(i);
    }

    @NotNull
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
            } else {
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
            if (item instanceof JsonStringNode && !(item instanceof JsonLongNode))
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
            if (item instanceof JsonStringNode && !(item instanceof JsonLongNode))
                builder.append(item.toString(0, 0));
            else
                builder.append(item.getContent());
            i--;
            if (i != 0)
                builder.append(",");
        }
        builder.append("]");
        return builder.toString();
    }

    public String toString(int lvl, int line_width) {
        StringBuilder rtn = new StringBuilder("[");
        String indent = "\t".repeat(lvl);
        String tail = String.format("\n%s]", indent);

        indent = "\t".repeat(lvl + 1);
        int i = this.content.size();
        int indent_len = indent.length() * 4;
        int l = indent_len; // trace used line width.
        List<String> temps = new ArrayList<>();
        int l2 = l;
        String temp;
        boolean append_new_line = true;
        for (JsonNode node : this.content) {
            temp = node.toString(lvl + 1, 0x7FFFFFFF);
            temps.add(temp);
            l2 += temp.length() + 2;
            if (l + temp.length() < line_width) {
                if (i == this.content.size()) {
                    rtn.append("\n");
                    rtn.append(indent);
                }
                l += temp.length();
                append_new_line = true;
            } else {
                if (append_new_line || node instanceof JsonStringNode
                        || indent_len + temp.length() < line_width
                ){
                    rtn.append("\n");
                    rtn.append(indent);
                    l = indent_len;
                }
                if (l + temp.length() >= line_width)
                    temp = node.toString(lvl + 1, line_width);
                if (temp.lastIndexOf('\n') != -1)
                    l += temp.length() - temp.lastIndexOf("\n");
                else
                    l += temp.length();
                append_new_line = false;
            }
            rtn.append(temp);
            i--;
            if (i != 0) {
                rtn.append(", ");
                l += 2;
            }
        }
        if (l2 < line_width) {
            rtn = new StringBuilder("[");
            if (!temps.isEmpty())
                rtn.append(temps.get(0));
            for (int it = 1; it < temps.size(); it++) {
                rtn.append(", ");
                rtn.append(temps.get(it));
            }
            rtn.append("]");
            return rtn.toString();
        }
        rtn.append(tail);
        return rtn.toString();
    }

    public void flatten(Map<String, String> map, String path) {
        for (JsonNode node : this.content) {
            node.flatten(map, path);
        }
    }

    public <T> void flatten(Map<String, T> map, String path, Function<String, T> func) {
        for (JsonNode node : this.content) {
            node.flatten(map, path, func);
        }
    }
}
