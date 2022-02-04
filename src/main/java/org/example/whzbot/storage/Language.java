package org.example.whzbot.storage;

import java.util.HashMap;

public class Language {
    public String lang;
    public Language father;

    public HashMap<String, String> global_variables = new HashMap<>();
    public HashMap<String, String> help_doc = new HashMap<>();
    public HashMap<String, String> card_translation = new HashMap<>();

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
        ProfileSaveAndLoad.loadMap(this.global_variables, dir_path + "/Variables.whz");
        ProfileSaveAndLoad.loadFlatJson(this.help_doc, dir_path + "/HelpDoc.whz");
        ProfileSaveAndLoad.loadMap(this.help_doc, dir_path + "/Tarot.whz");
        ProfileSaveAndLoad.loadMap(this.card_translation, dir_path + "/CardTrans.whz");
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
            rtn = this.getVariable("helpNotFound");
        return rtn;
    }
    public String getCardTranslate(String card) {
        String rtn = this.getCardRecursive(card);
        if (rtn == null)
            rtn = card;
        return rtn;
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
    }
}
