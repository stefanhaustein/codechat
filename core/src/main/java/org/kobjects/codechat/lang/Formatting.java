package org.kobjects.codechat.lang;

public final class Formatting {
    private Formatting() {
    }


    public static String numberToString(double d) {
        if (Double.isInfinite(d)) {
            return d < 0 ? "-\u221e" : "\u221e";
        }
        if (d == (long) d) {
            return String.valueOf((long) d);
        }
        return String.valueOf(d);
    }

    public static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                default:
                    if (c < ' ') {
                        sb.append("\\u00");
                        sb.append(Character.digit(c / 16, 16));
                        sb.append(Character.digit(c % 16, 16));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String toLiteral(Object value) {
        if (value instanceof Number) {
            return numberToString(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            return Formatting.quote((String) value);
        }
        if (value instanceof Instance) {
            Instance instance = (Instance) value;
            return instance.getType() + "#" + instance.getId();
        }
        return String.valueOf(value);
    }
}
