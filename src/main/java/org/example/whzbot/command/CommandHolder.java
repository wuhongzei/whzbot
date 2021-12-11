package org.example.whzbot.command;

import java.util.Map;

import org.example.whzbot.helper.StringHelper;

public class CommandHolder {
    protected String cmd_arg;
    protected int cursor;

    protected String cmd_name;
    protected String next_arg = null;
    protected Command cmd;

    private static Map<String, String> CMD_ALIAS; // str, str
    private static Map<String, String> DRAW_ALIAS; // str, str str
    private static Map<String, String> PRESET_ALIAS;

    public CommandHolder(String text, int index) {
        this.cmd_arg = text;
        this.cursor = index;

        this.cmd_name = this.getNextWord();
        if (CMD_ALIAS.containsKey(this.cmd_name))
            this.cmd_name = CMD_ALIAS.get(this.cmd_name);
        if (DRAW_ALIAS.containsKey(this.cmd_name)) {
            this.next_arg = DRAW_ALIAS.get(this.cmd_name);
            this.cmd_name = "draw";
        } else if (PRESET_ALIAS.containsKey(this.cmd_name)) {
            String[] cmd_and_arg = PRESET_ALIAS.get(this.cmd_name).split(" ");
            this.cmd_name = cmd_and_arg[0];
            this.next_arg = cmd_and_arg[1];
        }
        this.cmd = Command.fromString(this.cmd_name);
    }

    public CommandHolder(String text) {
        this(text, 0);
    }

    /**
     * Used when possible miss poll happens.
     * This method should be used when next_arg is empty.
     * But old value is returned in case.
     */
    public String setNextArg(String new_next) {
        String rtn = this.next_arg;
        this.next_arg = new_next;
        return rtn;
    }

    public String getName() {
        return this.cmd_name;
    }

    public Command getCmd() {
        return this.cmd;
    }

    public String getRest() {
        return this.next_arg == null ?
                this.cmd_arg.substring(this.cursor) :
                this.next_arg + " " + this.cmd_arg.substring(this.cursor);
    }

    /**
     * Get the next word in the argument.
     * Space, numbers will terminate the word.
     * Should check for has next before use.
     *
     * @return a string of word.
     */
    public String getNextWord() {
        if (this.next_arg == null) {
            int i = StringHelper.endOfWord(this.cmd_arg, this.cursor);
            String rtn = this.cmd_arg.substring(this.cursor, i);
            this.cursor = StringHelper.skipSpace(this.cmd_arg, i);
            return rtn;
        } else {
            String rtn = this.next_arg;
            this.next_arg = null;
            return rtn;
        }
    }

    /**
     * Return the next space-split string in arg.
     * If next_arg is not null, return next_arg.
     * Updates self so that only read once.
     *
     * @return a string of cmd argument.
     */
    public String getNextArg() {
        if (this.next_arg == null) {
            int i = this.cmd_arg.indexOf(' ', this.cursor);
            if (i < 0)
                i = this.cmd_arg.length();
            String rtn = this.cmd_arg.substring(this.cursor, i);
            this.cursor = StringHelper.skipSpace(this.cmd_arg, i);
            return rtn;
        } else {
            String rtn = this.next_arg;
            this.next_arg = null;
            return rtn;
        }
    }

    public String getNextInt() {
        int i = StringHelper.endOfInt(this.cmd_arg, this.cursor);
        String rtn = this.cmd_arg.substring(this.cursor, i);
        this.cursor = StringHelper.skipSpace(this.cmd_arg, i);
        return rtn;
    }

    public String getNextSign() {
        String rtn = this.cmd_arg.substring(this.cursor, this.cursor + 1);
        this.cursor = StringHelper.skipSpace(this.cmd_arg, this.cursor + 1);
        return rtn;
    }

    public String getNextSignedInt() {
        boolean signed = false;
        if (this.cmd_arg.charAt(this.cursor) == '-') {
            signed = true;
            this.cursor++;
        }
        int i = StringHelper.endOfInt(this.cmd_arg, this.cursor);
        String rtn = signed ? "-" : "";
        rtn += i == cursor ? "0" : this.cmd_arg.substring(this.cursor, i);
        this.cursor = StringHelper.skipSpace(this.cmd_arg, i);
        return rtn;
    }

    public String getNextFloat() {
        boolean signed = false;
        if (this.cmd_arg.charAt(this.cursor) == '-') {
            signed = true;
            this.cursor++;
        } else if (this.cmd_arg.charAt(this.cursor) == '+') {
            this.cursor++;
        }
        int i = StringHelper.endOfInt(this.cmd_arg, this.cursor);
        String rtn = signed ? "-" : "";
        rtn += i == cursor ? "0" : this.cmd_arg.substring(this.cursor, i);
        if (i < this.cmd_arg.length() && this.cmd_arg.charAt(i) == '.') {
            this.cursor = i + 1;
            if (this.isNextInt()) {
                i = StringHelper.endOfInt(this.cmd_arg, this.cursor);
                rtn = rtn + '.' + this.cmd_arg.substring(this.cursor, i);
                this.cursor = StringHelper.skipSpace(this.cmd_arg, i);
            }
        } else {
            this.cursor = i;
        }
        return rtn;
    }

    public boolean hasNext() {
        return this.cursor != this.cmd_arg.length() || this.next_arg != null;
    }

    public boolean isNextWord() {
        if (this.next_arg != null)
            return true;
        else
            return this.hasNext() &&
                    !StringHelper.isDigit(this.cmd_arg.charAt(this.cursor)) &&
                    !StringHelper.isSign(this.cmd_arg.charAt(this.cursor));
    }

    public boolean isNextInt() {
        if (this.next_arg != null)
            return !this.next_arg.isEmpty() && StringHelper.isDigit(this.next_arg.charAt(0));
        else
            return this.hasNext() && StringHelper.isDigit(this.cmd_arg.charAt(this.cursor));
    }

    public boolean isNextSign() {
        if (this.next_arg != null)
            return false;
        else
            return this.hasNext() && StringHelper.isSign(this.cmd_arg.charAt(this.cursor));
    }

    public boolean isNextSignedInt() {
        if (this.next_arg != null)
            return false;
        else {
            return this.hasNext() && (
                    StringHelper.isDigit(this.cmd_arg.charAt(this.cursor)) ||
                            this.cmd_arg.charAt(this.cursor) == '-'
            );
        }
    }

    public static boolean isCommand(String text) {
        return text.length() > 2 &&
                (text.charAt(0) == '.' || text.charAt(0) == '\u3002') &&
                (text.charAt(1) != '.' && text.charAt(1) != '\u3002');
    }

    public static void setCmdAlias(Map<String, String> cmd_map) {
        CMD_ALIAS = cmd_map;
    }

    public static void setDrawAlias(Map<String, String> draw_map) {
        DRAW_ALIAS = draw_map;
    }

    public static void setPresetAlias(Map<String, String> preset_map) {
        PRESET_ALIAS = preset_map;
    }
}
