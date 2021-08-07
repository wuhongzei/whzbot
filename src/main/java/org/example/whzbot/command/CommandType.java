package org.example.whzbot.command;

public enum CommandType {
    ADMIN(1),
    GROUP(7),
    GENERAL(8),
    DICE(2),
    TAROT(3),
    SIMCHAT(4),
    MCSERVER(5),
    MATH(6),
    NOTIFY(9);

    private final int id;
    CommandType(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
