package org.example.whzbot.storage.json;

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

    /*
    * Get a node from a path.
    * Param: a string with dot-split names.
    *   path = "" iff get return self.
    *   path = a.b return root.a.b
    *   list node can be accessed as a[i], while return a node named a[i].
    * Return: a node correspond to path, null if no node match.
    * */
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
}
