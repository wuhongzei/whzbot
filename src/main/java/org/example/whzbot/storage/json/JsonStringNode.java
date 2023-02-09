package org.example.whzbot.storage.json;

import org.example.whzbot.helper.StringHelper;

import java.util.Map;
import java.util.function.Function;

public class JsonStringNode extends JsonNode {
    protected String content;

    public JsonStringNode(String name) {
        this(name, name);
    }

    public JsonStringNode(String name, String content) {
        super(name);
        this.content = content;
    }
    public JsonStringNode(String name, String content, boolean sanitized) {
        super(name);
        if (sanitized)
            this.content = StringHelper.enSenString_(content);
        else
            this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    public String toString() {
        return String.format("\"%s\":\"%s\"", this.name, this.content);
    }

    public String toString(int lvl, int line_width) {
        return String.format("\"%s\"", StringHelper.deSenString_(this.content));
    }

    public void flatten(Map<String, String> map, String path) {
        if (this.name.isEmpty() || path.isEmpty())
            map.put(path + this.name, this.content);
        else
            map.put(String.format("%s.%s", path, this.name), this.content);
    }

    public <T> void flatten(Map<String, T> map, String path, Function<String, T> func) {
        if (this.name.isEmpty() || path.isEmpty())
            map.put(path + this.name, func.apply(this.content));
        else
            map.put(String.format("%s.%s", path, this.name), func.apply(this.content));
    }
}
