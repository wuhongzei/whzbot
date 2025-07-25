package org.example.whzbot.data;

import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

import java.util.HashMap;
import java.util.UUID;

public class User implements IUser {
    boolean modified = false;

    long id;
    Character character = null;
    String lang = null;
    String name;
    HashMap<String, String> setting = new HashMap<>();
    HashMap<String, JsonNode> storage = new HashMap<>();

    public User() {
        this.id = 0;
    }

    public User(long id) {
        this.id = id;
    }

    @Override
    public long getId() {
        return this.id;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {
        if (this.character != null) {
            String char_name = this.character.getName();
            return char_name.isEmpty() ? this.name : char_name;
        }
        return this.name;
    }

    public Character setCharacter(Character new_character) {
        Character old = this.character;
        this.character = new_character;
        this.modified = true;
        return old;
    }

    public Character getCharacter() {
        if (this.character == null)
            this.initCharacter();
        return this.character;
    }

    public UUID getCharacterUUID() {
        if (this.character == null)
            return null;
        return this.character.getUUID();
    }

    public String getLang() {
        return this.lang != null ? this.lang : "zh_cn";
    }

    public void setLang(String l) {
        this.lang = l;
        this.modified = true;
    }

    public boolean hasChanged() {
        return this.modified;
    }

    public void setSaved() {
        this.modified = false;
    }

    public void initCharacter() {
        this.character = Pool.getCharacter();
        this.modified = true;
    }

    public boolean containSetting(String path) {
        return this.setting.containsKey(path);
    }

    public String getSetting(String path, String val) {
        String rtn = this.setting.get(path);
        if (rtn != null)
            return rtn;
        rtn = GlobalVariable.DEFAULT_USER_SETTING.get(path);
        return rtn == null ? val : rtn;
    }

    public int getSetting(String path, int val) {
        String rtn = this.setting.get(path);
        if (rtn != null)
            return Integer.parseInt(rtn);
        rtn = GlobalVariable.DEFAULT_USER_SETTING.get(path);
        try {
            return rtn == null ? val : Integer.parseInt(rtn);
        } catch (RuntimeException e) {
            return val;
        }
    }

    @Override
    public void changeSetting(String path, String value) {
        this.setting.put(path, value);
        this.modified = true;
    }

    public String removeSetting(String path) {
        this.modified = true;
        return this.setting.remove(path);
    }

    @Override
    public String getStorage(String path) {
        JsonNode node = this.storage.get(path);
        if (node != null)
            return node.getContent();
        else return "";
    }

    @Override
    public void setStorage(String path, String value) {
        this.storage.put(path, new JsonStringNode("", value));
    }

    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();

        //rtn.add(new JsonLongNode("id", String.valueOf(this.id)));
        if (this.character != null)
            rtn.add(new JsonStringNode(
                    "character",
                    this.character.getUUID().toString())
            );
        if (this.lang != null)
            rtn.add(new JsonStringNode("lang", this.lang));

        JsonObjectNode setting_node = new JsonObjectNode("setting");
        Json.reconstruct(
                this.setting, setting_node,
                (String val) -> new JsonStringNode("", val)
        );
        rtn.add(setting_node);
        return rtn;
    }

    public void fromJson(JsonObjectNode json) {
        JsonNode character_node = json.get("character");
        if (character_node instanceof JsonStringNode) {
            this.character = Pool.getCharacter(
                    UUID.fromString(character_node.getContent()));
        }
        this.lang = Json.readString(json, "lang", null);

        JsonNode node = json.get("setting");
        if (node instanceof JsonObjectNode) {
            node.setName("");
            node.flatten(this.setting, "");
        }
    }
}
