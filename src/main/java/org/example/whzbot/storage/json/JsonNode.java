package org.example.whzbot.storage.json;

import java.util.Map;
import java.util.function.Function;

public class JsonNode {
    protected String name;

    protected JsonNode() {
        this.name = "";
    }

    protected JsonNode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String new_name) {
        this.name = new_name;
    }

    /**
     * Get a node from a path.
     * Param: a string with dot-split names.
     * path = "" iff get return self.
     * path = a.b return root.a.b
     * list node can be accessed as a[i], while return a node named a[i].
     * Return: a node correspond to path, null if no node match.
     */
    public JsonNode get(String path) {
        return path.isBlank() ? this : null;
    }

    public String getContent() {
        return "";
    }

    public boolean add(JsonNode node) {
        return false;
    }

    public boolean isEmpty() {
        return true;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        return String.format("\"%s\"", this.name);
    }

    public void flatten(Map<String, String> map, String path) {
        if (this.name.isEmpty() || path.isEmpty())
            map.put(path + this.name, "");
        else
            map.put(String.format("%s.%s", path, this.name), "");
    }

    public <T> void flatten(Map<String, T> map, String path, Function<String, T> func) {
        if (this.name.isEmpty() || path.isEmpty())
            map.put(path + this.name, func.apply(""));
        else
            map.put(String.format("%s.%s", path, this.name), func.apply(""));
    }
}
