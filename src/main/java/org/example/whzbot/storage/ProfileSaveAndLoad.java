package org.example.whzbot.storage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.HashMap;

import net.mamoe.mirai.utils.MiraiLogger;

import org.example.whzbot.helper.StringHelper;
import org.example.whzbot.storage.json.JsonLoader;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;

public class ProfileSaveAndLoad {
    public static MiraiLogger logger;
    private static boolean has_logger = false;

    private final static String[] charsets = {
            "gbk", "UTF-8", "windows-1253", "ISO-8859-7"
    };

    public static void loadMap(HashMap<String, String> map, String path) {
        File file = new File(path);
        Charset charset = detectCharset(file);
        if (charset == null)
            charset = Charset.defaultCharset();

        String file_content;
        try {
            FileInputStream stream = new FileInputStream(file);
            file_content = new String(stream.readAllBytes(), charset);
            stream.close();
        } catch (IOException e) {
            System.out.println(e.toString());
            file_content = "";
        }
        int index = 0;
        int end_line = 0;
        while (index < file_content.length()) {
            end_line = file_content.indexOf('\n', index);
            if (end_line == -1)
                end_line = file_content.length();
            String line;
            int i = file_content.indexOf("//", index);
            if (i > 0 && i < end_line)
                line = file_content.substring(index, i).trim();
            else
                line = file_content.substring(index, end_line).trim();
            if (line.contains("=")) {
                String[] names = line.split("=");
                if (names.length == 2)
                    map.put(
                            names[0],
                            StringHelper.enSenString(names[1])
                    );
                else
                    map.put(names[0], "");
            }
            index = end_line + 1;
        }
    }

    public static void saveMap(HashMap<String, String> map, String path) {
        try {
            FileWriter writer = new FileWriter(path);
            for (String key : map.keySet()) {
                writer.write(String.format("%s=%s\n", key, StringHelper.deSenString(map.get(key))));
            }
        } catch (IOException e) {
            errorLog(e.toString());
        }
    }

    public static JsonObjectNode loadJson(String path) {
        JsonLoader loader;
        try {
            loader = new JsonLoader(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new JsonObjectNode();
        }
        JsonNode rtn = loader.load();
        if (rtn instanceof JsonObjectNode) {
            return (JsonObjectNode) rtn;
        } else
            return new JsonObjectNode();
    }

    public static void saveJson(JsonNode json, String path) {
        try {
            FileWriter writer = new FileWriter(path);
            writer.write(json.getContent());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /**
     * Load a json data file and flatten all nodes into a map.
     * @param map A str-str mapping.
     * @param path Path to json file
     * @implNote Loads nothing if file is missing
     * @implNote flattened name follows json.get structure.
     */
    public static void loadFlatJson(HashMap<String, String> map, String path) {
        JsonNode root = loadJson(path);
        root.flatten(map, "");
    }

    public static void assertDir(String path, String usage) {
        File file = new File(path);
        if (!file.isDirectory()) {
            if (!file.mkdir()) {
                System.out.printf("Failed to save %s: cannot create dir", usage);
            }
        }
    }

    /**
     * @author Georgios Migdos <cyberpython@gmail.com>
     */
    public static Charset detectCharset(File f) {
        Charset charset = null;

        for (String charsetName : charsets) {
            try {
                BufferedInputStream input = new BufferedInputStream(new FileInputStream(f));
                charset = Charset.forName(charsetName);
                CharsetDecoder decoder = charset.newDecoder();
                decoder.reset();

                byte[] buffer = new byte[512];
                boolean identified = false;
                while ((input.read(buffer) != -1) && (!identified)) {
                    try {
                        decoder.decode(ByteBuffer.wrap(buffer));
                        identified = true;
                    } catch (CharacterCodingException ignored) {
                    }
                }
                input.close();

                if (!identified) {
                    charset = null;
                }
            } catch (Exception e) {
                return null;
            }
            if (charset != null) {
                break;
            }
        }

        return charset;
    }

    public static void setLogger(MiraiLogger logger_in) {
        logger = logger_in;
        has_logger = true;
    }

    public static void log(String info) {
        if (has_logger)
            logger.info(info);
    }

    public static void errorLog(String info) {
        if (has_logger)
            logger.error(info);
    }
}
