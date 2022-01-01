package org.example.whzbot.data;

import org.example.whzbot.storage.GlobalVariable;

import java.util.UUID;

public class Member implements IUser {
    User user;
    long group_id;

    public Member(long group_id, User user) {
        this.user = user;
        this.group_id = group_id;
    }

    public Member(long group_id, long id) {
        this.user = new User(id);
        this.group_id = group_id;
    }

    @Override
    public long getId() {
        return this.user.getId();
    }

    @Override
    public void setName(String n) {
        this.user.setName(n);
    }

    @Override
    public String getName() {
        return this.user.getName();
    }

    @Override
    public String getNickName() {
        return this.user.getNickName();
    }

    @Override
    public Character setCharacter(Character new_character) {
        return this.user.setCharacter(new_character);
    }

    @Override
    public Character getCharacter() {
        return this.user.getCharacter();
    }

    @Override
    public UUID getCharacterUUID() {
        return this.user.getCharacterUUID();
    }

    @Override
    public String getLang() {
        return this.user.getLang();
    }

    @Override
    public void setLang(String l) {
        this.user.setLang(l);
    }

    @Override
    public boolean hasChanged() {
        return this.user.hasChanged();
    }

    @Override
    public void initCharacter() {
        this.user.initCharacter();
    }

    @Override
    public boolean containSetting(String path) {
        return this.user.containSetting(path);
    }

    @Override
    public String getSetting(String path, String val) {
        String rtn = Pool.getGroup(this.group_id).getSetting(path);
        if (rtn != null)
            return rtn;
        if (this.user.containSetting(path))
            return this.user.getSetting(path, val);
        rtn = GlobalVariable.DEFAULT_GROUP_SETTING.get(path);
        return rtn == null ? val : rtn;
    }

    @Override
    public int getSetting(String path, int val) {
        String rtn = Pool.getGroup(this.group_id).getSetting(path);
        if (rtn == null)
            rtn = GlobalVariable.DEFAULT_GROUP_SETTING.get(path);
        if (this.user.containSetting(path))
            return this.user.getSetting(path, val);
        try {
            return rtn == null ? val : Integer.parseInt(rtn);
        } catch (RuntimeException e) {
            return val;
        }
    }

    @Override
    public void changeSetting(String path, String value) {
        Pool.getGroup(this.group_id).changeSetting(path, value);
    }

    public String removeSetting(String path) {
        return Pool.getGroup(this.group_id).removeSetting(path);
    }

    @Override
    public String getStorage(String path) {
        return this.user.getStorage(path);
    }

    @Override
    public void setStorage(String path, String value) {
        this.user.setStorage(path, value);
    }
}
