package org.example.whzbot.storage;

import org.example.whzbot.helper.RandomHelper;
import org.example.whzbot.storage.json.*;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;

public class Language {
    public String lang;
    public Language father;

    public HashMap<String, String> global_variables = new HashMap<>();
    public HashMap<String, String> help_doc = new HashMap<>();
    public HashMap<String, String> card_translation = new HashMap<>();
    public HashMap<String, String[]> names = new HashMap<>();

    private final static Language DUMMY = new DummyLanguage();

    public Language(String name, Language father) {
        this.lang = name;
        this.father = father;
    }
    public Language(String name) {
        this.lang = name;
        this.father = Language.DUMMY;
    }
    public void setFather(Language father) {
        this.father = father;
    }
    public void loadFromDist(String dir_path) {
        ProfileSaveAndLoad.loadFlatJson(this.global_variables, dir_path + "/Variables.whz");
        ProfileSaveAndLoad.loadFlatJson(this.help_doc, dir_path + "/HelpDoc.whz");
        ProfileSaveAndLoad.loadMap(this.help_doc, dir_path + "/Tarot.whz");
        ProfileSaveAndLoad.loadMap(this.card_translation, dir_path + "/CardTrans.whz");
        this.loadNames(dir_path + "/Names.whz");
    }

    public String getVariable(String var) {
        String rtn = this.getVariableRecursive(var);
        if (rtn == null)
            rtn = var;
        return rtn;
    }
    public String getHelpDoc(String var) {
        String rtn = this.getHelpRecursive(var);
        if (rtn == null)
            rtn = this.getVariable("help.not_found");
        return rtn;
    }
    public String getCardTranslate(String card) {
        String rtn = this.getCardRecursive(card);
        if (rtn == null)
            rtn = card;
        return rtn;
    }
    public String getRandomName(String lang) {
        String[] deck_set = this.getNameSetRecursive(lang);
        if (deck_set == null)
            return "lang_not_support";
        String str_name = RandomHelper.drawFromArray(deck_set);

        int index_front = str_name.indexOf('{');
        int index_back;
        while (index_front != -1) {
            index_front = str_name.indexOf('{');
            index_back = str_name.indexOf('}', index_front + 1);
            if (index_back == -1)
                break;

            String direction = str_name.substring(index_front + 1, index_back);
            deck_set = this.getNameSetRecursive(direction);
            if (deck_set == null)
                return "lang_not_support";
            str_name = str_name.replace(
                    str_name.substring(index_front, index_back + 1),
                    RandomHelper.drawFromArray(deck_set));

        }
        return str_name;
    }

    protected String getVariableRecursive(String var) {
        String rtn = this.global_variables.get(var);
        if (rtn == null)
            rtn = this.father.getVariableRecursive(var);
        return rtn;
    }
    protected String getHelpRecursive(String var) {
        String rtn = this.help_doc.get(var);
        if (rtn == null)
            rtn = this.father.getHelpRecursive(var);
        return rtn;
    }
    /**
    * Translates a card, if translation exists.
    * @return The translated string, if exist,
    *       otherwise, the original string.
    * */
    protected String getCardRecursive(String card) {
        String rtn = this.card_translation.get(card);
        if (rtn == null)
            rtn = this.father.getCardRecursive(card);
        return rtn;
    }
    protected String[] getNameSetRecursive(String lang_name) {
        String[] rtn = this.names.get(lang_name);
        if (rtn == null)
            rtn = this.father.getNameSetRecursive(lang_name);
        return rtn;
    }

    public static Language getLanguage(String lang_name) {
        if (GlobalVariable.LANGUAGES.containsKey(lang_name))
            return GlobalVariable.LANGUAGES.get(lang_name);
        return Language.DUMMY;
    }

    private static class DummyLanguage extends Language {
        protected DummyLanguage() {
            super("dummy", null);
        }
        protected String getVariableRecursive(String var) {
            return null;
        }
        protected String getHelpRecursive(String var) {
            return null;
        }
        protected String getCardRecursive(String card) {
            return null;
        }
        protected String[] getNameSetRecursive(String lang_name) {
            return null;
        }
    }

    private void loadNames(String path) {
        JsonObjectNode json = new JsonObjectNode();
        try {
            JsonLoader loader = new JsonLoader(path);
            json = (JsonObjectNode) loader.load();
        } catch (FileNotFoundException ignored) {
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

                this.names.put(list_node.getName(), cards.toArray(new String[0]));
            }
        }
    }
}
