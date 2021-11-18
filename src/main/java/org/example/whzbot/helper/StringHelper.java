package org.example.whzbot.helper;

public class StringHelper {
    // Return the index of next digit or white space char in given string.
    // str.substring(index, rtn) should provide a valid word.
    public static int endOfWord(String str, int index) {
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
        while (index < str.length()  &&
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
    public static boolean isSpace(char c) { return c == ' ' || c == '\n' || c == '\r';}

    public static String deSenString(String str) {
        return str.replaceAll("=", "\\=")
                .replaceAll("|", "\\|")
                .replaceAll("\n", "\\n")
                .replaceAll("\t", "\\t")
                .replaceAll("\\\\", "\\s");
    }
    public static String enSenString(String str) {
        return str.replaceAll("\\\\=", "=")
                .replaceAll("\\\\\\|", "|")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\s", "\\");
    }

    public static String parseRichText(String rich, String type) {
        int pos = rich.indexOf('[');
        String target = String.format("\"mirai:%s\":", type);
        if (target.equals(rich.substring(pos+1, pos+target.length()+1))) {
            return rich.substring(
                    pos+target.length()+1,
                    rich.indexOf(']', pos)
            );
        }
        return "";
    }
}
