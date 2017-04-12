package org.kobjects.codechat.tree;

import java.util.List;
import org.kobjects.codechat.Environment;

public abstract class Node {

    public abstract Object eval(Environment environment);

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
