package org.example.whzbot.storage.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class JsonObjectNode extends JsonNode implements Collection<JsonNode> {
    protected HashSet<JsonNode> content;

    public JsonObjectNode() {
        super();
        this.content = new HashSet<>();
    }

    public JsonObjectNode(String name) {
        super(name);
        this.content = new HashSet<>();
    }

    public JsonObjectNode(String name, JsonNode[] contents) {
        super(name);
        this.content = new HashSet<>(Arrays.asList(contents));
    }

    public boolean add(JsonNode node) {
        return this.content.add(node);
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
    public boolean removeAll(Collection<?> collection) {
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return false;
    }

    @Override
    public void clear() {

    }

    public JsonNode get(String path) {
        if (path.isBlank())
            return this;
        int dot_index = path.indexOf('.');
        int bracket_index = path.indexOf('[');
        dot_index = dot_index != -1 && bracket_index != -1 ?
                Math.min(dot_index, bracket_index) : Math.max(dot_index, bracket_index);
        String node_name;
        if (dot_index != -1) {
            node_name = path.substring(0, dot_index);
        } else
            node_name = path;
        for (JsonNode node : this.content) {
            if (node.getName().equals(node_name)) {
                return dot_index != -1 ?
                        node.get(path.substring(dot_index + 1)) : node.get("");
            }
        }
        return null;
    }

    public String getContent() {
        StringBuilder rtn = new StringBuilder("{");
        int i = this.content.size();
        for (JsonNode node : this.content) {
            rtn.append(node.toString());
            i--;
            if (i != 0)
                rtn.append(",");
        }
        rtn.append("}");
        return rtn.toString();
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

    public String toString() {
        StringBuilder rtn = new StringBuilder(String.format("\"%s\":{", this.name));
        int i = this.content.size();
        for (JsonNode node : this.content) {
            rtn.append(node.toString());
            i--;
            if (i != 0)
                rtn.append(",");
        }
        rtn.append("}");
        return rtn.toString();
    }

    public String toString(int lvl, int line_width) {
        if (this.isEmpty())
            return "{}";
        StringBuilder rtn = new StringBuilder("{");
        String tail = String.format("\n%s}", "\t".repeat(lvl));
        String indent = "\t".repeat(lvl + 1);

        int i = this.content.size();
        int l = indent.length(); // trace used line width.
        String temp;
        List<String> temps = new ArrayList<>();
        int l2 = 0;
        for (JsonNode node : this.content) {
            temp = node.toString(lvl + 1, 0x7FFFFFFF);
            if (l2 < line_width) {
                temps.add(String.format("\"%s\": %s", node.getName(), temp));
                l2 += node.getName().length() + temp.length() + 4;
            }
            String node_name = String.format("\"%s\": ", node.getName());
            if (l + temp.length() + node.getName().length() < line_width) {
                if (i == this.content.size()) {
                    rtn.append("\n");
                    rtn.append(indent);
                }
                rtn.append(node_name);
                l += temp.length() + node.getName().length() + 5;
            } else {
                temp = node.toString(lvl + 1, line_width);
                if (node instanceof JsonStringNode) {
                    rtn.append("\n");
                    rtn.append(indent);
                    rtn.append(node_name);
                    l = indent.length() + node.getName().length() + 4;
                } else {
                    if (l + node.getName().length() + 6 >= line_width) {
                        rtn.append("\n");
                        rtn.append(indent);
                        rtn.append(node_name);
                        l = indent.length();
                    } else {
                        if (i == this.content.size()) {
                            rtn.append("\n");
                            rtn.append(indent);
                        }
                        rtn.append(node_name);
                    }
                }
                l += temp.length() - temp.lastIndexOf("\n");
            }
            rtn.append(temp);
            i--;
            if (i != 0)
                rtn.append(", ");
            l += temp.length() + temp.lastIndexOf('\n');
        }
        if (l2 < line_width) {
            rtn = new StringBuilder("{");
            rtn.append(temps.get(0));
            for (int it = 1; it < temps.size(); it++) {
                rtn.append(", ");
                rtn.append(temps.get(it));
            }
            rtn.append("}");
            return rtn.toString();
        }
        rtn.append(tail);
        return rtn.toString();
    }

    public void flatten(Map<String, String> map, String path) {
        if (!(this.name.isEmpty() || path.isEmpty()))
            path = String.format("%s.%s", path, this.name);
        else
            path = path + this.name;
        for (JsonNode node : this.content) {
            node.flatten(map, path);
        }
    }

    public <T> void flatten(Map<String, T> map, String path, Function<String, T> func) {
        if (!(this.name.isEmpty() || path.isEmpty()))
            path = String.format("%s.%s", path, this.name);
        else
            path = path + this.name;
        for (JsonNode node : this.content) {
            node.flatten(map, path, func);
        }
    }

    @Override
    public Iterator<JsonNode> iterator() {
        return this.content.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.content.toArray();
    }

    @Override
    public <T> T[] toArray(T[] ts) {
        return this.content.toArray(ts);
    }
}
