package org.example.whzbot.storage.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

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
        }
        else
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

    public void flatten(Map<String, String> map, String path) {
        if (!(this.name.isEmpty() || path.isEmpty()))
            path = String.format("%s.%s", path, this.name);
        else
            path = path + this.name;
        for (JsonNode node : this.content) {
            node.flatten(map, path);
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
