package org.kobjects.codechat.tree;

import java.util.List;
import org.kobjects.codechat.Environment;

public abstract class Node {

    public abstract Object eval(Environment environment);

    public void assign(Environment environment, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }
}
