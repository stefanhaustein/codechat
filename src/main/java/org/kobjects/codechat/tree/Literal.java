package org.kobjects.codechat.tree;

import org.kobjects.codechat.Environment;

public class Literal extends Node {
    final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    public Object eval(Environment environment) {
        return value;
    }

    public String toString() {
        return String.valueOf(value);
    }

}
