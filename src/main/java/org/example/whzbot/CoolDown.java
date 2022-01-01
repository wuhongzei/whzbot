package org.example.whzbot;

import java.util.HashMap;

public class CoolDown {
    private static final HashMap<String, Long> TIMER = new HashMap<>();
    private static long last_update = 0;

    public static void update() {
        if (TIMER.isEmpty()) {
            last_update = System.currentTimeMillis();
            return;
        }
        long time = System.currentTimeMillis();
        time = time - last_update;
        if (time < 1000)
            return;

        long cool_down;
        for (String key : TIMER.keySet().toArray(new String[0])) {
            cool_down = TIMER.remove(key);
            if (cool_down > time)
                TIMER.put(key, cool_down - time);
        }
        last_update += time;
    }

    public static void setCoolDown(String key_in, long cd) {
        update();
        TIMER.put(key_in, cd);
    }

    public static long checkCoolDown(String key_in) {
        Long time = TIMER.get(key_in);
        return time == null ? 0 : time;
    }

    public static boolean isCool(String key_in) {
        update();
        return !TIMER.containsKey(key_in);
    }
}
