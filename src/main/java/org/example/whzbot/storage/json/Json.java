package org.example.whzbot.storage.json;

import org.example.whzbot.helper.StringHelper;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;

public class Json {
    public static JsonObjectNode fromString(String json_str) {
        int i = StringHelper.skipSpace(json_str, 0);
        if (json_str.charAt(i) != '{') {
            return null;
        }
        JsonObjectNode root = new JsonObjectNode();
        LinkedList<JsonNode> cursors = new LinkedList<>();

        cursors.addFirst(root);

        boolean stack_kept;
        boolean has_comma = true;

        while (!cursors.isEmpty()) {
            i = StringHelper.skipWhite(json_str, i + 1);

            stack_kept = true;
            while (stack_kept && i < json_str.length()) {
                if (json_str.charAt(i) == '}') {
                    if (has_comma && !cursors.getFirst().isEmpty())
                        System.err.printf("Extra comma before %d, line %d\n",
                                i, StringHelper.count(json_str, '\n', 0, i)
                        );

                    cursors.removeFirst();

                    stack_kept = false;
                } else if (json_str.charAt(i) == ',') {
                    i = StringHelper.skipWhite(json_str, i + 1);
                    has_comma = true;
                } else if (json_str.charAt(i) == '"') {
                    if (!has_comma) {
                        System.err.printf("Missing comma before %d, line %d\n",
                                i, StringHelper.count(json_str, '\n', 0, i)
                        );
                    }

                    int j = StringHelper.endOfString(json_str, i + 1);
                    String node_name = json_str.substring(i + 1, j);

                    j = json_str.indexOf(':', j + 1);
                    if (j == -1) { // If node name missing right quote.
                        System.err.printf("Unenclosed quote from %d, line %d\n",
                                i, StringHelper.count(json_str, '\n', 0, i)
                        );

                        j = json_str.indexOf(':', i + 1);
                        node_name = json_str.substring(i + 1, j);
                    }

                    i = StringHelper.skipWhite(json_str, j + 1);
                    if (json_str.charAt(i) == '"') {
                        j = StringHelper.endOfString(json_str, i + 1);

                        cursors.getFirst().add(
                                new JsonStringNode(node_name, json_str.substring(i + 1, j))
                        );
                        i = j;

                        has_comma = false;
                    } else if (json_str.charAt(i) == '{') {
                        JsonObjectNode new_node = new JsonObjectNode(node_name);
                        cursors.getFirst().add(new_node);
                        cursors.addFirst(new_node);

                        has_comma = true;
                    } else if (json_str.charAt(i) == '[') {
                        JsonListNode new_node = new JsonListNode(node_name);

                        i = StringHelper.skipWhite(json_str, i + 1);
                        while (json_str.charAt(i) != ']') {
                            if (json_str.charAt(i) == ',')
                                i = StringHelper.skipWhite(json_str, i + 1);
                            else if (json_str.charAt(i) == '"') {
                                j = StringHelper.endOfString(json_str, i + 1);
                                new_node.add(new JsonStringNode(json_str.substring(i + 1, j)));
                                i = getIndexes(json_str, j);
                            } else {
                                j = getIndexes(json_str, i);
                                new_node.add(new JsonStringNode(json_str.substring(i, j).trim()));
                                i = j;
                            }
                        }
                        cursors.getFirst().add(new_node);

                        has_comma = false;
                    } else {
                        j = json_str.indexOf(',', i + 1);
                        if (j == -1)
                            j = json_str.indexOf('}', i + 1);
                        cursors.getFirst().add(new JsonStringNode(
                                node_name,
                                json_str.substring(i, j).trim()
                        ));
                        i = j - 1;

                        has_comma = false;
                    }
                    i = StringHelper.skipWhite(json_str, i + 1);
                } else {
                    i++;
                }
            }
            if (stack_kept) {
                System.err.println("Braces not enclosed.");
                return root;
            }
        }
        return root;
    }

    private static int getIndexes(String json_str, int k) {
        int i = json_str.indexOf(',', k + 1);
        int j = json_str.indexOf(']', k + 1);
        if (i == -1) {
            return j;
        } else if (j == -1)
            return i;
        return Math.min(i, j);
    }

    public static class TypeNotMatchException extends RuntimeException {
        public TypeNotMatchException(String e) {
            super(e);
        }
    }

    public static class NodeNotFoundException extends RuntimeException {
        public NodeNotFoundException(String e) {
            super(e);
        }
    }

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
