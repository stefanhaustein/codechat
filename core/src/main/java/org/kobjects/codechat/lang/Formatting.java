package org.kobjects.codechat.lang;

import java.util.Arrays;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.expressionparser.ExpressionParser;

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

    public static void exceptionToString(AnnotatedStringBuilder asb, Throwable e) {
        if (e instanceof Printable) {
            ((Printable) e).print(asb, Printable.Flavor.DEFAULT);
        } else if (e instanceof ExpressionParser.ParsingException) {
            asb.append(e.getMessage());
        } else {
            asb.append(e.toString());
        }
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

    public static void toLiteral(AnnotatedStringBuilder asb, Object value) {
        if (value instanceof Number) {
            asb.append(numberToString(((Number) value).doubleValue()));
        } else if (value instanceof String) {
            asb.append(Formatting.quote((String) value));
        } else if (value instanceof Instance) {
            Instance instance = (Instance) value;
            asb.append(instance.toString(), new EntityLink(instance));
        } else if (value instanceof Instance) {
            Instance instance = (Instance) value;
            asb.append(instance.getType() + "#" + instance.getId(), new EntityLink(instance));
        } else if (value instanceof Documented) {
            asb.append(String.valueOf(value), new DocumentedLink((Documented) value));
        } else {
            asb.append(String.valueOf(value));
        }
    }

    public static AnnotatedCharSequence toLiteral(Object value) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        toLiteral(asb, value);
        return asb.build();
    }

    public static String space(int i) {
        char[] chars = new char[i];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }

    public static String toString(Object o) {
        return o instanceof Number ? numberToString(((Number) o).doubleValue()) : String.valueOf(o);
    }
}
