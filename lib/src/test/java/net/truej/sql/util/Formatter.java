package net.truej.sql.util;

public class Formatter {
    public static String pretty(Object o) {
        var s = o.toString();
        var sb = new StringBuilder();
        var deep = 0;

        for (var i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '[') {
                deep++;
                sb.append("[\n");
                sb.append(" ".repeat(Math.max(0, deep * 2)));
            } else if (s.charAt(i) == ']') {
                deep--;
                sb.append("]\n");
                sb.append(" ".repeat(Math.max(0, deep * 2)));
            } else
                sb.append(s.charAt(i));
        }

        return sb.toString();
    }
}
