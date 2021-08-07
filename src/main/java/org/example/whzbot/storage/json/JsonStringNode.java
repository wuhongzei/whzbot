package org.example.whzbot.storage.json;

public class JsonStringNode extends JsonNode {
    protected String content;

    public JsonStringNode(String name) {
        super(name);
    }
    public JsonStringNode(String name, String content) {
        super(name);
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return String.format("\"%s\":\"%s\"", this.name, this.content);
    }
}
