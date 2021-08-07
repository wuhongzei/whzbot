package org.example.whzbot.data;

import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

import java.util.HashMap;
import java.util.UUID;

public class Character {
    boolean modified = false;

    String name;
    String nick_name = null;
    HashMap<String, Integer> skills = new HashMap<>();
    String image_path = null;
    String rule = null;

    UUID uuid;

    public Character() {
        name = "";
        this.uuid = randomUUID();
    }

    public Character(String name) {
        this.name = name;
        this.uuid = randomUUID();
    }

    public Character(UUID uuid) {
        this.name = "";
        this.uuid = uuid;
    }

    public int setSkill(String skill_name, int value) {
        Integer i = this.skills.put(skill_name, value);
        this.modified = true;
        return i == null ? -1 : i;
    }

    public boolean hasSkill(String skill_name) {
        return this.skills.containsKey(skill_name);
    }

    public int getSkill(String skill_name) {
        Integer i = this.skills.get(skill_name);
        return i == null ? -1 : i;
    }

    public int delSkill(String skill_name) {
        Integer i = this.skills.remove(skill_name);
        this.modified = this.modified || i != null;
        return i == null ? -1 : i;
    }

    public void clrSkill() {
        this.skills.clear();
    }

    public void setRule(String new_rule) {
        this.rule = new_rule;
        this.modified = true;
    }

    public String setImage(String path) {
        String old = this.image_path;
        this.image_path = path;
        return old;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public String getName() {
        if (this.nick_name != null)
            return this.nick_name;
        return this.name;
    }

    public boolean hasChanged() {
        return this.modified;
    }

    public void setSaved() {
        this.modified = false;
    }

    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();
        rtn.add(new JsonStringNode("name", this.name));
        if (this.nick_name != null)
            rtn.add(new JsonStringNode("nick_name", this.nick_name));
        if (this.rule != null)
            rtn.add(new JsonStringNode("rule", this.rule));
        JsonObjectNode skill_node = new JsonObjectNode("skills");
        for (String skill_name : this.skills.keySet()) {
            skill_node.add(new JsonLongNode(
                    skill_name, String.valueOf(this.skills.get(skill_name))
            ));
        }
        rtn.add(skill_node);

        return rtn;
    }

    public void fromJson(JsonObjectNode json) {
        JsonNode node = json.get("name");
        this.name = node instanceof JsonStringNode ? node.getContent() : "";
        node = json.get("nickname");
        this.nick_name = node instanceof JsonStringNode ? node.getContent() : null;
        node = json.get("rule");
        this.rule = node instanceof JsonStringNode ? node.getContent() : null;

        node = json.get("skills");
        if (node instanceof JsonObjectNode) {
            for (JsonNode sub_node : (JsonObjectNode) node) {
                if (sub_node instanceof JsonLongNode) {
                    this.skills.put(
                            sub_node.getName(),
                            Integer.parseInt(sub_node.getContent())
                    );
                }
            }
        }
    }

    public static UUID randomUUID() {
        UUID uuid = UUID.randomUUID();
        while (Pool.containUUID(uuid))
            uuid = UUID.randomUUID();
        return uuid;
    }
}
