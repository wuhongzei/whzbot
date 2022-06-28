package org.example.whzbot.storage.json;

import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.storage.ProfileSaveAndLoad;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;

public class JsonLoader {

    private final File file; // Stores either readable file or a directory
    private String str;
    private int index;

    public JsonLoader(String path) throws FileNotFoundException {
        this.file = new File(path);
        if (!this.file.exists()) {
            throw new FileNotFoundException(path);
        }
    }

    public JsonLoader(String json_str, String name) {
        this.file = null;
        this.str = json_str;
    }

    /*
     * Loads a json tree from json file or a directory.
     * return null if file not found.
     */
    public JsonNode load() {
        if (this.file != null) {
            if (this.file.isDirectory())
                return null;
            FileInputStream stream;
            try {
                stream = new FileInputStream(file);
                Charset charset = ProfileSaveAndLoad.detectCharset(file);
                if (charset != null)
                    this.str = new String(stream.readAllBytes(), charset);
                else
                    this.str = new String(stream.readAllBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.str.isBlank())
            return new JsonObjectNode();
        // indicates location of last read from buffer
        this.index = 0;

        return loadFromStr("");
    }

    /*
     * Create a json node given name,
     * and read content from string.
     */
    private JsonNode loadFromStr(String name) {
        this.index = StringHelper.skipWhite(this.str, this.index);
        if (this.str.charAt(this.index) == '{') {
            JsonObjectNode root = new JsonObjectNode(name);
            boolean has_comma = true;
            this.index = StringHelper.skipWhite(this.str, this.index + 1);

            while (this.index < this.str.length()) {
                if (this.str.charAt(this.index) == '}') {
                    if (has_comma && !root.isEmpty())
                        this.warnExtraComma();

                    return root;
                } else if (this.str.charAt(this.index) == ',') {
                    if (has_comma) {
                        warnExtraComma();
                    }
                    this.index = StringHelper.skipWhite(this.str, this.index + 1);
                    has_comma = true;
                } else if (this.str.charAt(this.index) == '"') {
                    if (!has_comma) {
                        warnMissingComma();
                    }

                    int j = StringHelper.endOfString(this.str, this.index + 1);
                    String node_name = this.str.substring(this.index + 1, j);

                    j = this.str.indexOf(':', j + 1);
                    if (j == -1) { // If node name missing right quote.

                        j = this.str.indexOf(':', this.index + 1);
                        if (j > 0) {
                            node_name = this.str.substring(this.index + 1, j);
                            System.err.printf(
                                    "Unenclosed quote from %d, line %d at file \"%s\"\n",
                                    this.index,
                                    StringHelper.count(this.str, '\n', 0, this.index),
                                    this.file.getPath()
                            );
                        } else {
                            System.err.printf("Missing content %d, line %d at file \"%s\"\n",
                                    this.index,
                                    StringHelper.count(this.str, '\n', 0, this.index),
                                    this.file.getPath()
                            );
                            return new JsonNode(node_name);
                        }
                    }

                    this.index = StringHelper.skipWhite(this.str, j + 1);
                    root.add(this.loadFromStr(node_name));
                    has_comma = false;
                    this.index = StringHelper.skipWhite(this.str, this.index + 1);
                } else {
                    this.index++;
                }
            }
            return root;
        } else if (this.str.charAt(this.index) == '[') {
            this.index = StringHelper.skipWhite(this.str, this.index + 1);
            JsonListNode node = new JsonListNode(name);
            boolean has_comma = true;
            int count = 0;
            while (this.index < this.str.length()) {
                if (this.str.charAt(index) == ']') {
                    if (has_comma && !node.isEmpty()) {
                        warnExtraComma();
                    }
                    return node;
                } else if (this.str.charAt(index) == ',') {
                    if (has_comma) {
                        warnExtraComma();
                    }
                    has_comma = true;
                } else {
                    if (!has_comma) {
                        warnMissingComma();
                    }
                    node.add(this.loadFromStr(String.format("%s[%d]", name, count)));
                    has_comma = false;
                    count++;
                }
                this.index = StringHelper.skipWhite(this.str, this.index + 1);
            }
            return node;
        } else if (this.str.charAt(this.index) == '"') {
            int j = StringHelper.endOfString(this.str, this.index + 1);
            JsonStringNode rtn = new JsonStringNode(
                    name,
                    this.str.substring(this.index + 1, j)
            );
            this.index = j;
            return rtn;
        } else {
            // This block of code looks bad.
            // It searches for the nearest comma or bracket.
            int a = this.str.indexOf(',', this.index + 1);
            int b = this.str.indexOf('}', this.index + 1);
            int c = this.str.indexOf(']', this.index + 1);
            int j;
            // if either a, b, c is -1, max ignores it.
            j = a != -1 && b != -1 ? Math.min(a, b) : Math.max(a, b);
            j = j != -1 && c != -1 ? Math.min(j, c) : Math.max(j, c);

            if (j == -1) {
                System.err.printf("Unenclosed content %d, line %d at file \"%s\"\n",
                        this.index,
                        StringHelper.count(this.str, '\n', 0, this.index),
                        this.file.getPath()
                );
                j = this.str.length();
            }
            String content = this.str.substring(this.index, j).trim();
            try {
                Float.parseFloat(content);
                this.index = j - 1;
                return new JsonLongNode(name, content);
            } catch (NumberFormatException e) {
                this.index = j - 1;
                return new JsonStringNode(name, content);
            }
        }
    }

    private void warnMissingComma() {
        System.err.printf("Missing comma before %d, line %d at file \"%s\"\n",
                this.index,
                StringHelper.count(this.str, '\n', 0, this.index),
                this.file.getPath()
        );
    }

    private void warnExtraComma() {
        System.err.printf("extra comma at %d, line %d at file \"%s\"\n",
                this.index,
                StringHelper.count(this.str, '\n', 0, this.index),
                this.file.getPath()
        );
    }
}
