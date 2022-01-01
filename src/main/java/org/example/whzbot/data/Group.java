package org.example.whzbot.data;

import org.example.whzbot.storage.json.Json;
import org.example.whzbot.storage.json.JsonLongNode;
import org.example.whzbot.storage.json.JsonNode;
import org.example.whzbot.storage.json.JsonObjectNode;
import org.example.whzbot.storage.json.JsonStringNode;

import java.util.HashMap;


public class Group {
    boolean modified = false;

    long group_id;
    HashMap<Long, Member> member_pool;
    HashMap<String, String> group_setting;

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

    public String changeSetting(String name, String value) {
        String rtn = this.group_setting.put(name, value);
        this.modified = true;
        return rtn != null ? rtn : "";
    }

    public String getSetting(String path) {
        return this.group_setting.get(path);
    }

    public String removeSetting(String path) {
        return this.group_setting.remove(path);
    }

    public Member getMember(long id) {
        Member rtn = this.member_pool.get(id);
        return rtn != null ? rtn : new Member(this.group_id, Pool.getUser(id));
    }

    public boolean hasChanged() {
        return this.modified;
    }

    public void setSaved() {
        this.modified = false;
    }

    public JsonObjectNode toJson() {
        JsonObjectNode rtn = new JsonObjectNode();
        JsonObjectNode set_node = new JsonObjectNode("setting");
        Json.reconstruct(
                this.group_setting, set_node,
                (String str) -> new JsonStringNode("", str)
        );
        rtn.add(set_node);

        return rtn;
    }

    public void fromJson(JsonObjectNode json) {
        JsonNode node = json.get("setting");
        if (node instanceof JsonObjectNode) {
            for (JsonNode sub_node : (JsonObjectNode) node) {
                if (sub_node instanceof JsonStringNode) {
                    this.group_setting.put(
                            sub_node.getName(),
                            sub_node.getContent()
                    );
                }
            }
        }
    }
}
