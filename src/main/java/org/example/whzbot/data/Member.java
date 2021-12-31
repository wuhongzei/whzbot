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
        if (this.user.containSetting(path))
            return this.user.getSetting(path, "");
        String rtn = GlobalVariable.DEFAULT_GROUP_SETTING.get(path);
        return rtn == null ? val : rtn;
    }

    @Override
    public int getSetting(String path, int val) {
        if (this.user.containSetting(path))
            return Integer.parseInt(this.user.getSetting(path, ""));
        String rtn = GlobalVariable.DEFAULT_GROUP_SETTING.get(path);
        return rtn == null ? val : Integer.parseInt(rtn);
    }

}
