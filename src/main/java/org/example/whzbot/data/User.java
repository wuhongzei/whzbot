package org.example.whzbot.data;

import org.example.whzbot.storage.GlobalVariable;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

import java.util.UUID;

public class User {
    boolean modified = false;

    long id;
    Character character = null;
    String lang = null;
    String name;

    public User() {
        this.id = 0;
    }

    public User(long id) {
        this.id = id;
    }

    public void setName(String n) {
        this.name = n;
    }

    public String getName() {
        return this.name;
    }

    public String getNickName() {
        if (this.character != null){
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

    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();

        //rtn.add(new JsonLongNode("id", String.valueOf(this.id)));
        if (this.character != null)
            rtn.add(new JsonStringNode(
                    "character",
                    this.character.getUUID().toString())
            );

        return rtn;
    }

    public void fromJson(JsonObjectNode json) {
        JsonNode character_node = json.get("character");
        if (character_node instanceof JsonStringNode) {
            this.character = Pool.getCharacter(
                    UUID.fromString(character_node.getContent()));
        }
    }
}
