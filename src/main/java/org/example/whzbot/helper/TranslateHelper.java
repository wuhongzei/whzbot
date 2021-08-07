package org.example.whzbot.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.example.whzbot.storage.Language;

public class TranslateHelper {
    String str;
    List<TranslateHelper> replacements;
    int type;

    public TranslateHelper() {
        this("");
    }
    public TranslateHelper(String str, int type) {
        this(str, new ArrayList<>(), type);
    }
    public TranslateHelper(String str) {
        this(str, new ArrayList<>(),0);
    }
    public TranslateHelper(String str, List<TranslateHelper> list, int type) {
        this.str = str;
        this.replacements = list;
        this.type = type;
    }
    public TranslateHelper(String str, String[] lst, int type) {
        this.str = str;
        this.type = type;
        this.replacements = new ArrayList<>();
        for (String rpc : lst) {
            this.add(rpc);
        }
    }

    public TranslateHelper(String str, TranslateHelper[] lst, int type) {
        this(str, Arrays.asList(lst), type);
    }

    public void add(String rpc) {
        this.replacements.add(new TranslateHelper(rpc));
    }
    public void add(TranslateHelper rpc) {
        this.replacements.add(rpc);
    }

    public String translate(String lang_name) {
        if (this.type == 0)
            return this.str;
        else if (this.type == 4) {
            if (this.replacements == null || this.replacements.isEmpty())
                return "";
            StringBuilder builder = new StringBuilder();
            builder.append(this.replacements.get(0).translate(lang_name));
            for (int i = 1; i < this.replacements.size(); i++) {
                builder.append(this.str);
                builder.append(this.replacements.get(i).translate(lang_name));
            }
            return builder.toString();
        }
        Language lang = Language.getLanguage(lang_name);
        String translated;
        switch (this.type) {
            case 1:
                translated = lang.getVariable(this.str);
                break;
            case 2:
                translated = lang.getHelpDoc(this.str);
                int i = translated.indexOf('{');
                int j;
                while (i != -1) {
                    j = translated.indexOf('}', i);
                    if (j != -1) {
                        String sub_str = translated.substring(i + 1, j);
                        translated = translated.replaceAll(
                                String.format("\\{%s\\}", sub_str),
                                lang.getHelpDoc(sub_str)
                        );
                        i = translated.indexOf('{', j);
                    }
                    else {
                        i = -1;
                    }
                }
                break;
            case 3:
                translated = lang.getCardTranslate(this.str);
                break;
            default:
                translated = this.str;
        }

        int count = 0;
        for (TranslateHelper rpc : this.replacements) {
            translated = translated.replaceAll(
                    String.format("\\{%d\\}", count),
                    rpc.translate(lang_name)
            );
            count++;
        }

        return translated;
    }
}
