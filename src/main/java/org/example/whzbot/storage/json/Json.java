package org.example.whzbot.storage.json;

import org.example.whzbot.helper.StringHelper;

import java.util.LinkedList;

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

        while(!cursors.isEmpty()) {
            i = StringHelper.skipWhite(json_str, i + 1);

            stack_kept = true;
            while (stack_kept && i < json_str.length()) {
                if (json_str.charAt(i) == '}') {
                    if (has_comma && !cursors.getFirst().isEmpty())
                        System.err.printf("Extra comma before %d\n", i);

                    cursors.removeFirst();

                    stack_kept = false;
                }
                else if (json_str.charAt(i) == ',') {
                    i = StringHelper.skipWhite(json_str, i + 1);
                    has_comma = true;
                }
                else if (json_str.charAt(i) == '"') {
                    if (!has_comma) {
                        System.err.printf("Missing comma before %d\n", i);
                    }

                    int j = StringHelper.endOfString(json_str, i + 1);
                    String node_name = json_str.substring(i + 1, j);

                    j = json_str.indexOf(':', j + 1);
                    if (j == -1) { // If node name missing right quote.
                        System.err.printf("Unenclosed quote from %d\n", i);

                        j = json_str.indexOf(':', i + 1);
                        node_name = json_str.substring(i + 1, j);
                    }

                    i = StringHelper.skipWhite(json_str, j + 1);
                    if (json_str.charAt(i) == '"') {
                        j = StringHelper.endOfString(json_str, i + 1);

                        cursors.getFirst().add(
                                new JsonStringNode(node_name, json_str.substring(i+1, j))
                        );
                        i = j;

                        has_comma = false;
                    }
                    else if (json_str.charAt(i) == '{') {
                        JsonObjectNode new_node = new JsonObjectNode(node_name);
                        cursors.getFirst().add(new_node);
                        cursors.addFirst(new_node);

                        has_comma = true;
                    }
                    else if (json_str.charAt(i) == '[') {
                        JsonListNode new_node = new JsonListNode(node_name);

                        i = StringHelper.skipWhite(json_str, i + 1);
                        while(json_str.charAt(i) != ']') {
                            if (json_str.charAt(i) == ',')
                                i = StringHelper.skipWhite(json_str, i + 1);
                            else if (json_str.charAt(i) == '"') {
                                j = StringHelper.endOfString(json_str, i + 1);
                                new_node.add(new JsonStringNode(json_str.substring(i + 1, j)));
                                i = getIndexes(json_str, j);
                            }
                            else{
                                j = getIndexes(json_str, i);
                                new_node.add(new JsonStringNode(json_str.substring(i, j).trim()));
                                i = j;
                            }
                        }
                        cursors.getFirst().add(new_node);

                        has_comma = false;
                    }
                    else {
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
                }
                else {
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
        }
        else if (j == -1)
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


}
