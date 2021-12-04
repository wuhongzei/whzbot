package org.example.whzbot.data;

import org.example.whzbot.storage.ProfileSaveAndLoad;
import org.example.whzbot.storage.json.JsonObjectNode;

import java.util.HashMap;
import java.util.UUID;

import static org.example.whzbot.JavaMain.working_dir;


public class Pool {
    static HashMap<Long, User> USER_POOL = new HashMap<>();
    static HashMap<Long, Group> GROUP_POOL = new HashMap<>();
    static HashMap<UUID, Character> CHARACTER_POOL = new HashMap<>();

    public static Group getGroup(long id) {
        Group rtn = GROUP_POOL.get(id);
        if (rtn == null) {
            String path = getGroupPath(id);
            rtn = new Group(id);
            JsonObjectNode root = ProfileSaveAndLoad.loadJson(path);
            rtn.fromJson(root);
            GROUP_POOL.put(id, rtn);
        }
        return rtn;
    }

    public static User getUser(long id) {
        User rtn = USER_POOL.get(id);
        if (rtn == null) {
            String path = getUserPath(id);
            rtn = new User(id);
            JsonObjectNode root = ProfileSaveAndLoad.loadJson(path);
            rtn.fromJson(root);
            USER_POOL.put(id, rtn);
        }
        return rtn;
    }

    public static Member getMember(long group_id, long user_id) {
        Group gp = getGroup(group_id);
        return gp.getMember(user_id);
    }

    public static Character getCharacter() {
        Character rtn = new Character();
        CHARACTER_POOL.put(rtn.getUUID(), rtn);
        return rtn;
    }

    public static Character getCharacter(UUID uuid) {
        Character rtn = CHARACTER_POOL.get(uuid);
        if (rtn == null) {
            String path = getCharacterPath(uuid);
            rtn = new Character(uuid);
            JsonObjectNode root = ProfileSaveAndLoad.loadJson(path);
            rtn.fromJson(root);
            CHARACTER_POOL.put(uuid, rtn);
        }
        return rtn;
    }

    public static boolean containUUID(UUID uuid) {
        return CHARACTER_POOL.containsKey(uuid);
    }

    public static String getUserPath(long id) {
        return working_dir + "\\data\\users\\" +
                id % 256 +
                "\\" +
                id +
                ".json";
    }

    public static String getGroupPath(long id) {
        return working_dir + "\\data\\groups\\" +
                id +
                ".json";
    }

    public static String getCharacterPath(UUID uuid) {
        String str_uuid = uuid.toString();
        return working_dir + "\\data\\characters\\" +
                str_uuid.substring(0, 2) +
                "\\" +
                str_uuid +
                ".json";
    }

    public static void unloadGroups() {
        ProfileSaveAndLoad.assertDir(working_dir + "\\data\\groups", "groups");
        for(long id : GROUP_POOL.keySet()) {
            Group gp = GROUP_POOL.get(id);
            if (gp.hasChanged()) {
                ProfileSaveAndLoad.saveJson(gp.toJson(), getGroupPath(id));
            }
        }
        GROUP_POOL.clear();
    }
    public static void unloadUsers() {
        ProfileSaveAndLoad.assertDir(working_dir + "\\data\\users", "users");
        for(long id : USER_POOL.keySet()) {
            User user = USER_POOL.get(id);
            String path = getUserPath(id);
            if (user.hasChanged()) {
                ProfileSaveAndLoad.assertDir(
                        path.substring(0, path.lastIndexOf('\\')),
                        "user_" + id
                );

                ProfileSaveAndLoad.saveJson(user.toJson(), path);
                user.setSaved();
            }
        }
        USER_POOL.clear();
    }
    public static void unloadCharacters() {
        ProfileSaveAndLoad.assertDir(working_dir + "\\data\\characters", "characters");
        for(UUID uuid : CHARACTER_POOL.keySet()) {
            Character character = CHARACTER_POOL.get(uuid);
            String path = getCharacterPath(uuid);
            if (character.hasChanged()) {
                ProfileSaveAndLoad.assertDir(
                        path.substring(0, path.lastIndexOf('\\')),
                        "character_" + uuid
                );

                ProfileSaveAndLoad.saveJson(character.toJson(), path);
                character.setSaved();
            }
        }
        CHARACTER_POOL.clear();
    }
}
