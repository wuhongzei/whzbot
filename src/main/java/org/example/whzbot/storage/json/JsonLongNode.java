package org.example.whzbot.storage.json;

/*
 * content will be stored as string,
 * but the string must be able to convert to number
 */
public class JsonLongNode extends JsonStringNode{
    private double value;

    public JsonLongNode(String name) {
        super(name);
        this.value = 0;
    }
    public JsonLongNode(String name, String content) {
        super(name, content);
        this.value = Double.parseDouble(content);
    }
    public String getContent() {
        return this.content;
    }
    public String toString() {
        return String.format("\"%s\":%s", this.name, this.content);
    }
}
