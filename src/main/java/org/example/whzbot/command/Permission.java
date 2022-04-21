package org.example.whzbot.command;

public enum Permission {
    ANYONE(0),
    GROUP_MEMBER(1),
    GROUP_ADMIN(2),
    GROUP_OWNER(3),
    BOT_ADMIN(4),
    BOT_ADMIN_IN_GROUP(5),
    BOT_AND_GROUP_ADMIN(6),
    BOT_ADMIN_AS_GROUP_OWNER(7),
    BOT_OWNER(8),
    BOT_OWNER_IN_GROUP(9),
    BOT_OWNER_AS_GROUP_ADMIN(10),
    BOT_AND_GROUP_OWNER(11),
    ANY_ADMIN(12),
    ALL(13);

    private final int level;

    Permission(int l) {
        this.level = l;
    }

    public int getLevel() {
        return this.level;
    }

    public static boolean hasPermit(Permission require, Permission has) {
        int r = require.getLevel();
        int h = has.getLevel();

        if (r == 12)
            return h > 1;
        if (r == 13)
            return h > 4 && h % 4 > 1;
        if (r % 4 == 1 || h % 4 != 0)
            return h % 4 >= r % 4 && (h >> 2) >= (r >> 2);
        else
            return (h >> 2) >= (r >> 2);
    }

    public static Permission mergePermit(int group, int bot) {
        int level = group + bot * 4;
        for (Permission p : Permission.values()) {
            if (p.level == level) {
                return p;
            }
        }
        return Permission.ANYONE;
    }
}
