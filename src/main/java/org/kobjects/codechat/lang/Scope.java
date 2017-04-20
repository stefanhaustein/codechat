package org.kobjects.codechat.lang;

import java.util.Map;
import java.util.TreeMap;

public class Scope {
    public Environment environment;
    Scope parent;
    public Map<String, Variable> variables = new TreeMap<>();
    int nextIndex;

    public Scope(Environment environment) {
        this.environment = environment;
    }

    public Scope(Scope parent) {
        this(parent.environment);
        this.parent = parent;
    }

    public Variable resolve(String name) {
        return variables.get(name);
    }

    public void ensureVariable(String name, Type type) {
        Variable existing = variables.get(name);
        if (existing != null) {
            if (!existing.getType().isAssignableFrom(type)) {
                throw new RuntimeException("Can't assign " + type + " to variable of type " + existing.getType());
            }
        } else {
            variables.put(name, new Variable(name, type, nextIndex++));
        }
    }
}
