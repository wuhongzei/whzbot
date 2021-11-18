package org.example.whzbot.storage.json;

public class JsonBooleanNode extends JsonLongNode{
    public JsonBooleanNode(String name) {
        super(name);
    }

    public JsonBooleanNode(String name, String content) {
        super(name, content);
    }

    public JsonBooleanNode(String name, boolean content) {
        super(name, content ? "1" : "0");
    }
}
