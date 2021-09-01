package org.example.whzbot.command;


import org.example.whzbot.data.User;
import org.example.whzbot.data.Character;
import org.example.whzbot.helper.MinHeap;
import org.example.whzbot.helper.RandomHelper;

public class CommandHelper {
    /**
     * set a skill
     * sub arguments
     * clr if no other arg, clear all.
     * clr sk_name, equal to del sk_name
     * del sk_name, delete skill.
     * show show the skill value.
     * set optional
     * (+/-/= dice expression)
     * dice expression will be send to roll_dice,
     * if execution success, set skill value.
     * otherwise set value.
     *
     * @param user   user, used to find character.
     * @param holder command, assume ra.
     * @return space split string.
     * err error_code
     * clr 
     * del skill_name
     * show value skill_name
     * set new_value skill_name
     * set new_value old_value skill_name
     * mod new_value old_value(+-=)dice_expr skill_name
     */
    public static String set_skill(User user, CommandHolder holder) {
        Character character = user.getCharacter();
        String skill_name = null;
        if (holder.isNextWord()) {
            skill_name = holder.getNextWord();
            switch (skill_name) {
                case "clr":
                    if (!holder.hasNext()) {
                        if (character != null) {
                            character.clrSkill();
                        }
                        return "clr";
                    }
                case "del":
                    if (!holder.isNextWord())
                        return "err miss_skill";
                    skill_name = holder.getNextWord();
                    int i = character.delSkill(skill_name);
                    if (i == -1)
                        return "err no_skill";
                    return "del" + skill_name;
                case "show":
                    if (!holder.isNextWord())
                        return "err miss_skill";
                    skill_name = holder.getNextWord();
                    i = character.getSkill(skill_name);
                    return i != -1 ?
                            "show " + i + " " + skill_name : "err no_skill";
                case "set":
                    if (!holder.isNextWord())
                        return "err miss_skill";
                    skill_name = holder.getNextWord();
                    break;
                default:
                    break;
            }
        }
        // mod or set (if sign =)
        if (holder.isNextSign()) {
            if (skill_name == null)
                return "err miss_skill";
            String sign = holder.getNextSign();
            if (!character.hasSkill(skill_name) && !sign.equals("="))
                return "err no_skill";
            String[] dice_result = roll_dice(user, holder).split(" ");
            if (dice_result[0].charAt(0) == 'h')
                dice_result[0] = dice_result[0].substring(1);
            int mod_val;
            switch (dice_result[0]) {
                case "err":
                    return "err d_" + dice_result[1];
                case "rd_":
                case "rdk":
                case "rbp":
                    if (dice_result[dice_result.length - 1].indexOf('#') != -1)
                        return "err #not_allow";
                    mod_val = Integer.parseInt(dice_result[1]);
                    break;
                default:
                    return "err unknown";
            }

            int new_value;
            int old_value = character.getSkill(skill_name);
            StringBuilder builder = new StringBuilder();
            switch (sign) {
                case "+":
                    new_value = old_value + mod_val;
                    character.setSkill(skill_name, new_value);
                    builder.append("mod ");
                    builder.append(new_value);
                    break;
                case "-":
                    new_value = old_value - mod_val;
                    character.setSkill(skill_name, new_value);
                    builder.append("mod ");
                    builder.append(new_value);
                    break;
                case "=":
                    character.setSkill(skill_name, mod_val);
                    builder.append("set ");
                    builder.append(mod_val);
                    break;
                default:
                    return "err sign_not_allow";
            }
            builder.append(String.format(" %s%s%s ",
                    old_value, sign, dice_result[dice_result.length - 1]
            ));
            builder.append(skill_name);
            return builder.toString();
        }
        // set
        if (skill_name == null) {
            if (!holder.hasNext())
                return "err miss_skill";
            if (!holder.isNextInt())
                return "err not_name";
            skill_name = holder.getNextWord();
        }
        if (!holder.isNextInt())
            if (!character.hasSkill(skill_name))
                return "err no_skill";
            else
                return "show " + character.getSkill(skill_name) + " " + skill_name;

        int new_value = Integer.parseInt(holder.getNextInt());
        int old_value = character.setSkill(skill_name, new_value);
        return "set " + new_value + " " + old_value + " " + skill_name;
    }

    /**
     * Roll a dice.
     * sub arguments
     * (h) hidden, be read and marked, return h_
     * (round=1#), number of round.
     * (number=1), number of dice (per round)
     * (d dice=100), faces of dice, default varies.
     * (b bonus=0), bonus apply to 00-90, take max
     * (p -bonus=0), penalty apply to 00-09, take min
     * (k pick=0), pick some best/worst dice each round.
     *
     * @param user   not in use, just to formalize.
     * @param holder by calling this func, assume command .r
     * @return space separated string
     * err followed by error code.
     * rbp r =ori[b or p]
     * rdk r =1+2+3...
     * rd_ r =1+2+3...
     * if no error and hidden, h is at front.
     * for each dice value range[1,dice]
     * for each round, results split by ','
     * The last element must be a proper expression
     */
    public static String roll_dice(User user, CommandHolder holder) {
        // .r (h)(count=1#)(number=1)(d dice=100) (b bonus=0) (p -bonus=0) \
        //      (k pick=0)
        int round = 1;
        int number;
        boolean hidden = false;
        if (holder.isNextWord()) {
            String w = holder.getNextWord();
            if (w.equals("h") || w.equals("hidden")) {
                hidden = true;
            } else {
                w = holder.setNextArg(w);
                assert w == null;
            }
        }
        if (!holder.hasNext()) //.r|
            return "err arg_null";
        if (holder.isNextInt()) { //.r c
            number = Integer.parseInt(holder.getNextInt());
            if (!holder.hasNext()) //.r c|
                return "val " + number;
        } else { //.r
            number = 1;
        }
        String w;
        int dice = 100;
        int bonus = 0;
        int pick = 0;

        loop:
        while (holder.isNextWord()) {
            w = holder.getNextWord();
            switch (w) {
                case "d":
                    if (!holder.isNextInt())
                        return "err arg_after_d";
                    dice = Integer.parseInt(holder.getNextInt());
                    break;
                case "#":
                    round = number;
                    if (!holder.isNextInt()) {
                        number = 1;
                    } else {
                        number = Integer.parseInt(holder.getNextInt());
                    }
                    break;
                case "b":
                case "bonus":
                    if (!holder.isNextInt())
                        return "err arg_after_b";
                    bonus = Integer.parseInt(holder.getNextInt());
                    break;
                case "p":
                case "penalty":
                    if (!holder.isNextInt())
                        return "err arg_after_p";
                    bonus = -Integer.parseInt(holder.getNextInt());
                    break;
                case "k":
                    if (!holder.isNextSignedInt())
                        return "err arg_after_k";
                    pick = Integer.parseInt(holder.getNextSignedInt());
                    break;
                default:
                    holder.setNextArg(w);
                    break loop;
            }
        }
        if (number != 1 && bonus != 0)
            return "err arg_coexist_c_and_b/p";
        if (bonus != 0 && pick != 0)
            return "err arg_coexist_b/p_and_k";
        if (dice < 1)
            return "err arg_dice<0";
        if (round > 10)
            return "err arg_round_too_big";
        if (pick > number)
            pick = 0;

        boolean abbreviate = round * dice > 1000; // when false, show individual dices.

        String[] results = new String[round];
        String rtn;
        int count = 0;
        if (bonus != 0) { // .r count d dice b/p bonus|
            boolean is_bonus = bonus > 0; //bonus or penalty
            bonus = is_bonus ? bonus : -bonus;

            while (count < round) {
                results[count] = rollDiceWithBonus(dice, bonus, is_bonus);
                count++;
            }

            StringBuilder builder = new StringBuilder("rbp ");
            builder.append(results[0]);
            for (int i = 1; i < round; i++) {
                builder.append('\n');
                builder.append(results[i]);
            }
            if (round != 1) {
                if (is_bonus)
                    builder.append(
                            String.format(" %d#r%db%d", round, dice, bonus));
                else
                    builder.append(
                            String.format(" %d#r%dp%d", round, dice, bonus));
            } else {
                if (is_bonus)
                    builder.append(
                            String.format(" r%db%d", dice, bonus));
                else
                    builder.append(
                            String.format(" r%dp%d", dice, bonus));
            }
            rtn = builder.toString();

        } else if (pick != 0) { //.r (count) d (dice) k (pick)
            if (pick > 100)
                return "err pick_too_many";

            int pick_max = pick > 0 ? 1 : -1;
            pick = pick * pick_max;

            Integer[] picked = new Integer[pick];
            int i;
            int d;
            MinHeap<Integer> heap;
            while (count < round) {
                i = 0;
                for (; i < pick; i++) {
                    picked[i] = RandomHelper.dice(dice) * pick_max;
                }
                heap = new MinHeap<>(picked); // create min heap from array;
                for (; i < number; i++) {
                    d = RandomHelper.dice(dice) * pick_max;
                    if (d > heap.top()) { // update heap to keep max val;
                        heap.extract();
                        heap.add(d);
                    }
                }
                picked = heap.toArray(picked); // take heap content;
                int sum = 0;
                for (i = 0; i < pick; i++) {
                    picked[i] *= pick_max; // reflect sign again.
                    sum += picked[i];
                }

                StringBuilder builder = new StringBuilder(String.valueOf(sum));
                builder.append(" =");
                builder.append(picked[0]);
                for (i = 1; i < pick; i++) {
                    builder.append('+');
                    builder.append(picked[i]);
                }
                results[count] = builder.toString();
                count++;
            }
            StringBuilder builder = new StringBuilder("rdk ");
            builder.append(results[0]);
            for (i = 1; i < round; i++) {
                builder.append(',');
                builder.append(results[i]);
            }
            if (round != 1)
                builder.append(
                        String.format(" %d#%dd%dk%d", round, number, dice, pick * pick_max));
            else
                builder.append(String.format(" %dd%dk%d", number, dice, pick * pick_max));
            rtn = builder.toString();
        } else if (number > 20) { //.r (count>20) d (dice)
            for (int i = 0; i < round; i++)
                results[i] = String.valueOf(RandomHelper.dice(dice, number));
            StringBuilder builder = new StringBuilder("rd_ ");
            builder.append(results[0]);
            for (int i = 1; i < round; i++) {
                builder.append(',');
                builder.append(results[i]);
            }
            if (round != 1)
                builder.append(
                        String.format(" %d#%dd%d", round, number, dice));
            else
                builder.append(String.format(" %dd%d", number, dice));
            rtn = builder.toString();
        } else { //.r (count) d (dice)
            while (count < round) {
                int[] val = new int[number];
                int sum = 0;
                for (int i = 0; i < number; i++) {
                    val[i] = RandomHelper.dice(dice);
                    sum += val[i];
                }
                StringBuilder builder = new StringBuilder(String.valueOf(sum));
                if (number > 1) {
                    builder.append(" =");
                    builder.append(val[0]);
                    for (int i = 1; i < number; i++) {
                        builder.append('+');
                        builder.append(val[i]);
                    }
                } else
                    builder.append(' ');
                results[count] = builder.toString();
                count++;
            }

            StringBuilder builder = new StringBuilder("rd_ ");
            if (round != 1) {
                builder.append(results[0].replace(" ", ""));
                for (int i = 1; i < round; i++) {
                    builder.append('\n');
                    builder.append(results[i].replace(" ", ""));
                }
                builder.append(
                        String.format(" %d#%dd%d", round, number, dice));
            } else {
                builder.append(results[0]);
                builder.append(String.format(" %dd%d", number, dice));
            }
            rtn = builder.toString();
        }

        if (hidden)
            return "h" + rtn;
        else
            return rtn;
    }

    public static String roll_det(User user, CommandHolder holder) {
        int cutoff;
        Character character = user.getCharacter();
        if (holder.isNextInt()) {
            cutoff = Integer.parseInt(holder.getNextInt());
        } else if (holder.isNextWord()) {
            String skill_name = holder.getNextWord();
            cutoff = character.getSkill(skill_name);
            if (cutoff == -1)
                return "err no_skill";
        } else
            return "err miss_skill";
        int d = RandomHelper.hundred();
        return String.format("ra_ %d %d", d, cutoff);
    }

    private static String rollDiceWithBonus(int dice, int bonus, boolean is_bonus) {
        StringBuilder builder = new StringBuilder();

        int result = RandomHelper.dice(dice);

        if (bonus > 100) {
            if (is_bonus) {
                builder.append(result % 10 + 90);
                builder.append(" =");
                builder.append(result);
                builder.append("[bonus=9]");
            } else {
                builder.append(result % 10);
                builder.append(" =");
                builder.append(result);
                builder.append("[bonus=0]");
            }
        } else {
            int bonus_dice;

            int[] tens = new int[bonus];
            for (int i = 0; i < bonus; i++)
                tens[i] = RandomHelper.ten();

            bonus_dice = tens[0];
            if (is_bonus) {
                for (int i = 1; i < bonus && bonus_dice != 9; i++) {
                    if (bonus_dice < tens[i])
                        bonus_dice = tens[i];
                }

                builder.append(result < bonus_dice * 10 ?
                        result % 10 + bonus_dice * 10 : bonus_dice);
                builder.append(" =");
                builder.append(result);
                builder.append("[bonus=");
                if (bonus < 5) {
                    builder.append(tens[0]);
                    for (int i = 1; i < bonus; i++) {
                        builder.append(',');
                        builder.append(tens[i]);
                    }
                } else {
                    builder.append(bonus_dice);
                }
            } else {
                for (int i = 1; i < bonus && bonus_dice != 0; i++) {
                    if (bonus_dice > tens[i])
                        bonus_dice = tens[i];
                }

                builder.append(result > bonus_dice * 10 ?
                        result % 10 + bonus_dice * 10 : bonus_dice);
                builder.append(" =");
                builder.append(result);
                builder.append("[penalty=");
                if (bonus < 5) {
                    builder.append(tens[0]);
                    for (int i = 1; i < bonus; i++) {
                        builder.append(',');
                        builder.append(tens[i]);
                    }
                } else {
                    builder.append(bonus_dice);
                }
            }
            builder.append(']');
        }
        return builder.toString();
    }

    public static String san_check(User user, CommandHolder holder) {
        Character c = user.getCharacter();
        int san = c.getSkill("san");
        if (san == -1)
            return "err san_unset";

        String[] buffer;
        String succ_dice = roll_dice(user, holder);
        buffer = succ_dice.split(" ");
        if (buffer[0].equals("err")) {
            return "err " + buffer[1];
        }
        int s = Integer.parseInt(buffer[1]);
        String fail_dice;
        if (!holder.hasNext())
            return "err no_sec";
        if (!holder.isNextSign()) {
            fail_dice = roll_dice(user, holder);
        } else {
            String sign = holder.getNextSign();
            fail_dice = roll_dice(user, holder);
        }
        buffer = fail_dice.split(" ");
        if (buffer[0].equals("err")) {
            return "err " + buffer[1];
        }
        int f = Integer.parseInt(buffer[1]);

        int dice = RandomHelper.hundred();

        int new_san;
        if (dice <= san) {
            new_san = san - s;
            if (new_san < 0)
                new_san = 0;
            else if (new_san > 100)
                new_san = 100;
            c.setSkill("san", new_san);
            return String.format("suc %d %d %d %d", dice, san, s, new_san);
        } else {
            new_san = san - f;
            if (new_san < 0)
                new_san = 0;
            else if (new_san > 100)
                new_san = 100;
            c.setSkill("san", new_san);
            return String.format("fal %d %d %d %d", dice, san, f, new_san);
        }
    }

    /** Enhance (en) changes a skill by given value after one det.
     *
     * @param user User for character.
     * @param holder A command, format as following.
     *               .en skill_name (value) success_dice
     *               .en skill_name (value) fail/success.
     * @return Some space split string.
     *      err no_skill if no value and skill not set.
     *      err dice_err if any dice expr err.
     *      err out_bound if value set over 100 or below 0.
     *      suc change old_val new_val det_result dice_expr_result
     *      fal change old_val new_val det_result dice_expr_result
     */
    public static String enhance(User user, CommandHolder holder) {
        Character character = user.getCharacter();
        String skill_name;
        int skill_val;

        if (!holder.hasNext()) {
            return "err no_skill";
        }
        else if (holder.isNextWord()) {
            skill_name = holder.getNextWord();
            if (holder.isNextInt()) {
                skill_val = Integer.parseInt(holder.getNextInt());
                if (skill_val > 100) {
                    return "err out_bound";
                }
            }
            else {
                skill_val = character.getSkill(skill_name);
                if (skill_val == -1) {
                    return "err no_skill";
                }
            }
        }
        else {
            return "err no_skill";
        }

        String suc_sign;
        String suc_dice;
        String fal_sign;
        String fal_dice;
        if (holder.isNextSign()) {
            suc_sign = holder.getNextSign();
            suc_dice = roll_dice(user, holder);
            if (holder.isNextSign()) {
                fal_sign = suc_sign;
                fal_dice = suc_dice;
                if (holder.isNextSign()) {
                    suc_sign = holder.getNextSign();
                }
                else {
                    suc_sign = "+";
                }
                suc_dice = roll_dice(user, holder);
            }
            else {
                fal_sign = "+";
                fal_dice = "di 0 0";
            }
        } else {
            return "err dice_err";
        }

        int det = RandomHelper.hundred();

        if (det > skill_val) {
            String[] buffer = suc_dice.split(" ");
            if (buffer[0].equals("err")) {
                return "err dice." + buffer[1];
            }
            else if (buffer[0].indexOf('#') > 0) {
                return "err dice_err";
            }
            int s = Integer.parseInt(buffer[1]);

            if (suc_sign.equals("+")) {
                character.setSkill(skill_name, skill_val + s);
                return String.format("suc +%d %d %d %d %s",
                        s, skill_val, skill_val + s, det, buffer[2]);
            }
            else if (suc_sign.equals("-")) {
                character.setSkill(skill_name, skill_val - s);
                return String.format("suc -%d %d %d %d %s",
                        s, skill_val, skill_val - s, det, buffer[2]);
            }
            else {
                return "err dice_err";
            }
        } else {
            String[] buffer = suc_dice.split(" ");
            if (buffer[0].equals("err")) {
                return "err dice." + buffer[1];
            }
            else if (buffer[0].indexOf('#') > 0) {
                return "err dice_err";
            }
            int f = Integer.parseInt(buffer[1]);

            if (fal_sign.equals("+")) {
                character.setSkill(skill_name, skill_val + f);
                return String.format("suc +%d %d %d %d %s",
                        f, skill_val, skill_val + f, det, buffer[2]);
            }
            else if (fal_sign.equals("-")) {
                character.setSkill(skill_name, skill_val - f);
                return String.format("suc -%d %d %d %d %s",
                        f, skill_val, skill_val - f, det, buffer[2]);
            }
            else {
                return "err dice_err";
            }
        }
    }
}