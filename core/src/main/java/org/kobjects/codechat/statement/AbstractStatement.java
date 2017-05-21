package org.kobjects.codechat.statement;

public abstract class AbstractStatement implements Statement {

    final static Object KEEP_GOING = new Object();

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public static void indent(StringBuilder sb, int indent) {
        for (int i = 0; i < indent; i++) {
            sb.append(' ');
        }
    }
}
