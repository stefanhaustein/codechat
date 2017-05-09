package org.kobjects.codechat.lang;

import java.util.Map;
import java.util.TreeMap;

public class ParsingContext {
    public Environment environment;
    ParsingContext parent;
    public Map<String, LocalVariable> variables = new TreeMap<>();
    int[] nextIndex;

    public ParsingContext(Environment environment) {
        this.environment = environment;
        nextIndex = new int[1];
    }

    public ParsingContext(ParsingContext parent) {
        this(parent.environment);
        this.parent = parent;
        this.nextIndex = parent.nextIndex;
    }

    public LocalVariable resolve(String name) {
        return variables.get(name);
    }

    public void ensureVariable(String name, Type type) {
        LocalVariable existing = variables.get(name);
        if (existing != null) {
            if (!existing.getType().isAssignableFrom(type)) {
                throw new RuntimeException("Can't assign " + type + " to variable of type " + existing.getType());
            }
        } else {
            addVariable(name, type);
        }
    }

    public LocalVariable addVariable(String name, Type type) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Variable '" + name + "' exists already.");
        }
        LocalVariable variable = new LocalVariable(name, type, nextIndex[0]++);
        variables.put(name, variable);
        return variable;
    }

    public int getVarCount() {
        return nextIndex[0];
    }

    public EvaluationContext createEvaluationContext() {
        return new EvaluationContext(environment, getVarCount());
    }
}
