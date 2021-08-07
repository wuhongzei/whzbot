package org.example.whzbot.data;

import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;

import java.util.HashMap;


public class Group {
    boolean modified = false;

    long group_id;
    HashMap<Long, Member> member_pool;
    HashMap<String, Long> group_setting;

    public Group() {
        this.member_pool = new HashMap<>();
        this.group_setting = new HashMap<>();
        this.group_id = 0;
    }
    public Group(long id) {
        this.group_id = id;
        this.member_pool = new HashMap<>();
        this.group_setting = new HashMap<>();
    }

    public long changeSetting(String name, long value) {
        Long rtn = this.group_setting.put(name, value);
        this.modified = true;
        return rtn != null ? rtn : -1;
    }

    public Member getMember(long id) {
        Member rtn = this.member_pool.get(id);
        return rtn != null ? rtn : new Member(this.group_id, id);
    }

    public boolean hasChanged() {
        return this.modified;
    }
    public void setSaved() {
        this.modified = false;
    }

    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();
        JsonObjectNode set_node = new JsonObjectNode();
        for (String skill_name : this.group_setting.keySet()) {
            set_node.add(new JsonLongNode(
                    skill_name, String.valueOf(this.group_setting.get(skill_name))
            ));
        }
        rtn.add(set_node);

        return rtn;
    }
    public void fromJson(JsonObjectNode json) {
        JsonNode node = json.get("skills");
        if (node instanceof JsonObjectNode) {
            for (JsonNode sub_node : (JsonObjectNode) node) {
                if (sub_node instanceof JsonLongNode) {
                    this.group_setting.put(
                            sub_node.getName(),
                            Long.parseLong(sub_node.getContent())
                    );
                }
            }
        }
    }
}
