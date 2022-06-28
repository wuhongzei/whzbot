package org.example.whzbot.storage.json;

import java.util.Map;
import java.util.function.Function;

public class Json {

    public static int readInt(JsonObjectNode root, String path, int default_value) {
        JsonNode node = root.get(path);
        if (node instanceof JsonLongNode) {
            return Integer.parseInt(node.getContent());
        } else
            return default_value;
    }

    public static double readDouble(JsonObjectNode root, String path, double default_value) {
        JsonNode node = root.get(path);
        if (node instanceof JsonLongNode) {
            return Double.parseDouble(node.getContent());
        } else
            return default_value;
    }

    public static String readString(JsonObjectNode root, String path, String default_value) {
        JsonNode node = root.get(path);
        if (node instanceof JsonStringNode) {
            return node.getContent();
        } else
            return default_value;
    }

    public static <T> void reconstruct(
            Map<String, T> map, JsonObjectNode root,
            Function<T, JsonNode> func) {
        String path;
        JsonNode node;
        JsonObjectNode parent;
        int i;
        for (Map.Entry<String, T> entry : map.entrySet()) {
            parent = root;
            path = entry.getKey();
            i = path.indexOf('.');
            while (i >= 0) {
                node = parent.get(path.substring(0, i));
                if (!(node instanceof JsonObjectNode)) {
                    parent.remove(node);
                    node = new JsonObjectNode(path.substring(0, i));
                    parent.add(node);
                }
                parent = (JsonObjectNode) node;
                path = path.substring(i + 1);
                i = path.indexOf('.');
            }
            node = parent.get(path);
            if (node != null)
                parent.remove(node);
            node = func.apply(entry.getValue());
            node.setName(path);
            parent.add(node);
        }
    }
}
