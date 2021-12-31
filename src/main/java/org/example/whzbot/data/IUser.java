package org.example.whzbot.data;

import java.util.UUID;

public interface IUser {

    long getId();

    void setName(String n);

    String getName();

    String getNickName();

    Character setCharacter(Character new_character);

    Character getCharacter();

    UUID getCharacterUUID();

    String getLang();

    void setLang(String l);

    boolean hasChanged();

    void initCharacter();

    boolean containSetting(String setting);

    String getSetting(String setting, String default_value);

    int getSetting(String setting, int default_value);

}
