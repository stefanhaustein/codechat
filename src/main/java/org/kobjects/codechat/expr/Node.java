package org.kobjects.codechat.expr;

import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Evaluable;

public abstract class Node implements Evaluable {


    public void assign(Environment environment, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public abstract void toString(StringBuilder sb, int parentPrecedence);
}
