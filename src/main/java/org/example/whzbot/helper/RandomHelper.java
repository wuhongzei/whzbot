package org.example.whzbot.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class RandomHelper {
    public static Random rnd = new Random();

    public static float jrrpRandom(long id) {
        Calendar date = new GregorianCalendar();
        return new Random(id +
                date.get(Calendar.YEAR) * 328103 +
                date.get(Calendar.DAY_OF_YEAR) * 596489
        ).nextFloat();
    }

    public static int hundred() {
        return rnd.nextInt(100) + 1;
    }
    public static int ten() {
        return rnd.nextInt(10) + 1;
    }
    public static int dice(int bound) {
        return rnd.nextInt(bound) + 1;
    }
    public static int dice(int bound, int count) {
        if (count < 0)
            return 0;
        if (count > 4096) {
            double sum = 0;
            for (int i = 0; i < 4096; i++)
                sum += rnd.nextDouble();
            return (int) (sum * count / 4096 * (bound - 1)) + count;
        }
        else {
            int sum = 0;
            for (int i = 0; i < count; i++)
                sum += rnd.nextInt(bound) ;
            return sum + count;
        }
    }

    public static String drawFromArray(String[] array) {
        int index = rnd.nextInt(array.length);
        return array[index];
    }
    public static String[] drawFromArray(String[] array, int count) {
        List<String> list = new ArrayList<>(Arrays.asList(array));
        if (count > list.size())
            count = list.size();

        String[] rtn = new String[count];
        for (int i = 0; i < count; i++) {
            rtn[i] = list.remove(rnd.nextInt(list.size()));
        }
        return rtn;
    }
}
