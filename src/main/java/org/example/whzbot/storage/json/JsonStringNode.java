package org.example.whzbot.storage.json;

import java.util.Map;

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

    public void flatten(Map<String, String> map, String path) {
        if (this.name.isEmpty() || path.isEmpty())
            map.put(path + this.name, this.content);
        else
            map.put(String.format("%s.%s", path, this.name), this.content);
    }
}
