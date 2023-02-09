package org.example.whzbot.helper;

public class StringHelper {
    // Return the index of next digit or white space char in given string.
    // str.substring(index, rtn) should provide a valid word.
    public static int endOfWord(String str, int index) {
        if (!(index < str.length()))
            return index;
        char c = str.charAt(index);
        while (!isWhite(c) &&
                isNotDigit(c) &&
                !isSign(c)
        ) {
            index++;
            if (index < str.length())
                c = str.charAt(index);
            else
                return index;
        }
        return index;
    }

    // Return the index of next digit or white space char in given string.
    // str.substring(index, rtn) should provide a valid integer (in str).
    public static int endOfInt(String str, int index) {
        while (index < str.length() &&
                isDigit(str.charAt(index))) {
            index++;
        }
        return index;
    }

    /* Given a string and a index of ", find the other " that encloses a string.
    // \" should be escaped.
    // if no other valid " found, return length of string.
    */
    public static int endOfString(String str, int index) {
        while (index < str.length() && str.charAt(index) != '"') {
            index += str.charAt(index) == '\\' ? 2 : 1;
        }
        return index;
    }

    // Return the index of next none space char in given string.
    public static int skipSpace(String str, int index) {
        while (index < str.length() && str.charAt(index) == ' ')
            index++;
        return index;
    }

    /**
     * Find the index of closing bracket,
     * should support '' out, multiple layer{{}}, and escape char.
     *
     * @param str   input string.
     * @param index index of opening bracket.
     * @return index of the corresponding close bracket.
     */
    public static int encloseBracket(String str, int index) {
        char op = str.charAt(index);
        char cl = (char) (op + (op + 44) / 84 + op / 64);
        int layer = 1;
        boolean escape = false;
        while (layer > 0) {
            index++;
            if (index >= str.length())
                break;
            char c = str.charAt(index);
            if (c == cl && !escape) {
                layer--;
            } else if (c == op && !escape) {
                layer++;
            } else if (c == '\\') {
                escape = !escape;
            } else if ((c == '\'' || c == '"') && !escape) {
                index = encloseBracket(str, index);
            }
        }
        return index;
    }

    // Return the index of next none white space char in given string.
    public static int skipWhite(String str, int index) {
        while (index < str.length() && isWhite(str.charAt(index)))
            index++;
        return index;
    }

    public static boolean isWhite(char c) {
        return c == ' ' || c == '\r' || c == '\n' || c == '\t';
    }

    public static boolean isSign(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '=';
    }

    public static boolean isDigit(char c) {
        return !(c < '0' || c > '9');
    }

    public static boolean isNotDigit(char c) {
        return c < '0' || c > '9';
    }

    public static boolean isSpace(char c) {
        return c == ' ' || c == '\n' || c == '\r';
    }

    public static String deSenString(String str) {
        return str.replaceAll("\\\\", "\\\\\\\\")
                .replaceAll("\\|", "\\\\|")
                .replaceAll("\r\n", "\\\\n")
                .replaceAll("\n", "\\\\n")
                .replaceAll("\t", "\\\\t");
    }

    public static String pieceReplaceString(String str, String[] sans, String[] rpcs) {
        if (rpcs.length < sans.length) {
            String[] temp = new String[sans.length];
            System.arraycopy(rpcs, 0, temp, 0, rpcs.length);
            for (int i = rpcs.length; i < sans.length; i++)
                temp[i] = "";
            rpcs = temp;
        }
        int str_len = str.length();
        int[] marked = new int[str_len];
        int[] hits = new int[str_len];
        for (int i = 0; i < sans.length; i++) {
            int j = 0;
            while (marked[j] != 0)
                j += marked[j];
            int san_len = sans[i].length();
            int[] temp = new int[san_len];
            for (int t = 0; t < san_len; t++)
                temp[t] = san_len - t;
            while (j < str_len) {
                int hit = str.indexOf(sans[i], j);
                if (hit < 0) {
                    j = str_len;
                } else if (marked[hit] == 0) {
                    boolean flag = true;
                    for (int t = hit; t < hit + san_len; t++) {
                        if (marked[t] != 0) {
                            flag = false;
                            break;
                        }
                    }
                    if (flag) {
                        hits[hit] = i;
                        System.arraycopy(
                                temp, 0, marked,
                                hit, Math.min(san_len, str_len - hit)
                        );
                    }
                    j = hit + marked[hit];
                } else
                    j = hit + marked[hit];
            }
        }
        StringBuilder builder = new StringBuilder();
        int i = 0;
        int j = 0;
        while (i < str_len) {
            if (marked[i] != 0) {
                if (marked[j] == 0)
                    builder.append(str, j, i);
                builder.append(rpcs[hits[i]]);
                i += marked[i];
                j = i;
            } else
                i++;
        }
        if (j < str_len && marked[j] == 0)
            builder.append(str, j, str_len);
        return builder.toString();
    }

    public static String deSenString_(String str) {
        return pieceReplaceString(
                str,
                new String[]{"\r\n", "\r", "\n", "\\", "\t"},
                new String[]{"\\n", "\\n", "\\n", "\\\\", "\\t"}
        );
    }

    public static String enSenString_(String str) {
        return pieceReplaceString(
                str,
                new String[]{"\\n", "\\\\", "\\t"},
                new String[]{"\r\n", "\\", "\t"}
        );
    }

    public static String enSenString(String str) {
        return str.replaceAll("\\\\\\|", "|")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\\r", "\r")
                .replaceAll("\\\\\n", "\n")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\\\\\", "\\");
    }

    public static String parseRichText(String rich, String type) {
        int pos = rich.indexOf('[');
        String target = String.format("mirai:%s:", type);
        if (target.equals(rich.substring(pos + 1, pos + target.length() + 1))) {
            return rich.substring(
                    pos + target.length() + 1,
                    rich.indexOf(']', pos)
            );
        }
        return "";
    }

    public static int count(String str, char target, int beg, int end) {
        int rtn = 0;
        int index = str.indexOf(target, beg);
        while (index > -1 && index < end) {
            index = str.indexOf(target, index + 1);
            rtn++;
        }
        return rtn;
    }
}
