package org.example.whzbot.command;

public enum CommandType {
    ADMIN(1),
    DICE(2),
    TAROT(3),
    SIMCHAT(4),
    MCSERVER(5),
    MATH(6),
    GROUP(7),
    GENERAL(8),
    WEB(9),
    NOTIFY(10);

    private final int id;
    CommandType(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
}
