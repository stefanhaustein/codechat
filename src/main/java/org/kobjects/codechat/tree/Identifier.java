package org.kobjects.codechat.tree;

import org.kobjects.codechat.Environment;

public class Identifier extends Node {
    public final String name;

    public Identifier(String name) {
        this.name = name;
    }
    @Override
    public Object eval(Environment environment) {
        Object result = environment.variables.get(name);
        if (result == null) {
            throw new RuntimeException("Undefined identifier: " + name);
        }
        return result;
    }

    public void assign(Environment environment, Object value) {
        environment.variables.put(name, value);
    }

    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(name);
    }
}
