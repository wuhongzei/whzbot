package org.example.whzbot.helper;

public class DiceHelper {

    public static int rollSuccessLevel(int res, int rate, int rule) {
        switch (rule) {
            case 1:
                if (res == 100)return 0;
                if (res == 1 || res <= 5 && rate >= 50)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                if (rate >= 50 || res < 96)return 1;
                else return 0;
            case 2:
                if (res == 100)return 0;
                if (res <= 5 && res <= rate)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                if (res < 96)return 1;
                else return 0;
            case 3:
                if (res >= 96)return 0;
                if (res <= 5)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                else return 1;
            case 4:
                if (res == 100)return 0;
                if (res <= 5 && res <= rate / 10)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                if (rate >= 50 || res < 96 + rate/10)return 1;
                else return 0;
            case 5:
                if (res >= 99)return 0;
                if (res <= 2 && res < rate / 10)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                if (rate >= 50 || res < 96)return 1;
                else return 0;
            case 0:
            default :
                if (res == 100)return 0;
                if (res == 1)return 5;
                if (res <= rate / 5)return 4;
                if (res <= rate / 2)return 3;
                if (res <= rate)return 2;
                if (rate >= 50 || res < 96)return 1;
                else return 0;
        }
    }
    public static String rollResultName(int res, int rate, int rule) {
        int type = rollSuccessLevel(res, rate, rule);
        String name;
        switch (type) {
            case 0:
                name = "dice.fumble";
                break;
            case 1:
                name = "dice.failure";
                break;
            case 2:
                name = "dice.regular_success";
                break;
            case 3:
                name = "dice.hard_success";
                break;
            case 4:
                name = "dice.extreme_success";
                break;
            case 5:
                name = "dice.critical_success";
                break;
            default:
                name = "dice.unknown_result";
        }
        return name;
    }
}
