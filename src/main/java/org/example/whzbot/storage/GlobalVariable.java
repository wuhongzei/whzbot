package org.example.whzbot.storage;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

import org.example.whzbot.command.CommandHolder;
import org.example.whzbot.data.gacha.GachaPool;
import org.example.whzbot.storage.json.JsonListNode;
import org.example.whzbot.storage.json.JsonLoader;
import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

public class GlobalVariable {
    private static final HashMap<String, String> CMD_ALIAS = new HashMap<>(); // str, str
    private static final HashMap<String, String> DRAW_ALIAS = new HashMap<>(); // str, str str
    private static final HashMap<String, String> PRESET_ALIAS = new HashMap<>();

    public static final HashMap<String, String> LANGUAGE_LIST = new HashMap<>();
    public static final HashMap<String, Language> LANGUAGES = new HashMap<>();

    public static final HashMap<String, String[]> CARD_DECK = new HashMap<>();
    public static final HashMap<String, GachaPool> GACHA_POOL = new HashMap<>();

    public static final HashMap<String, String> DEFAULT_GROUP_SETTING = new HashMap<>();
    public static final HashMap<String, String> DEFAULT_USER_SETTING = new HashMap<>();

    // Methods to load.
    public static void loadCmdAlias(String path) {
        ProfileSaveAndLoad.loadMap(CMD_ALIAS, path);
        CommandHolder.setCmdAlias(CMD_ALIAS);
    }

    public static void loadDrawAlias(String path) {
        ProfileSaveAndLoad.loadMap(DRAW_ALIAS, path);
        CommandHolder.setDrawAlias(DRAW_ALIAS);
    }

    public static void loadPresetAlias(String path) {
        ProfileSaveAndLoad.loadMap(PRESET_ALIAS, path);
        CommandHolder.setPresetAlias(PRESET_ALIAS);
    }

    public static void loadLanguageList(String path) {
        ProfileSaveAndLoad.loadMap(LANGUAGE_LIST, path);
    }

    public static void loadLanguages(String base_path) {
        HashMap<Language, String> orphans = new HashMap<>();
        for (String lang_name : LANGUAGE_LIST.keySet()) {
            String father_name = LANGUAGE_LIST.get(lang_name);
            Language lang;
            if (LANGUAGES.containsKey(father_name)) {
                lang = new Language(lang_name, LANGUAGES.get(father_name));
            } else {
                lang = new Language(lang_name);
                orphans.put(lang, father_name);
            }
            lang.loadFromDist(base_path + "/" + lang_name);
            LANGUAGES.put(lang_name, lang);
        }
        for (Language lang : orphans.keySet()) {
            Language father = LANGUAGES.get(orphans.get(lang));
            if (father != null)
                lang.setFather(father);
        }
    }

    public static void loadCardDeck(String path) {
        JsonObjectNode json = new JsonObjectNode();
        try {
            JsonLoader loader = new JsonLoader(path);
            json = (JsonObjectNode) loader.load();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (JsonNode list_node : json) {
            if (list_node instanceof JsonListNode) {
                ArrayList<String> cards = new ArrayList<>();
                for (JsonNode item_node : (JsonListNode) list_node) {
                    if (item_node instanceof JsonStringNode)
                        cards.add(item_node.getContent());
                    else if (item_node instanceof JsonObjectNode) {
                        JsonNode weight_node = item_node.get("weight");
                        JsonNode card_node = item_node.get("card");
                        if (weight_node instanceof JsonLongNode &&
                                card_node instanceof JsonStringNode) {
                            int weight = (int) Double.parseDouble(
                                    weight_node.getContent());
                            String card = card_node.getContent();
                            for (int i = 0; i < weight; i++)
                                cards.add(card);
                        }
                    }
                }

                CARD_DECK.put(list_node.getName(), cards.toArray(new String[0]));
            }
        }
    }

    public static void loadGachaPool(String path) {
        JsonObjectNode json = new JsonObjectNode();
        try {
            JsonLoader loader = new JsonLoader(path);
            json = (JsonObjectNode) loader.load();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        for (JsonNode node : json) {
            if (node instanceof JsonListNode) {
                GachaPool pool = new GachaPool((JsonListNode) node);
                GACHA_POOL.put(pool.getName(), pool);
            }
        }
    }

    public static void loadDefaultSetting(String path_group, String path_user) {
        ProfileSaveAndLoad.loadFlatJson(DEFAULT_GROUP_SETTING, path_group);
        ProfileSaveAndLoad.loadFlatJson(DEFAULT_USER_SETTING, path_user);
    }
}
