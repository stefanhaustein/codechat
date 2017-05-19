package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class ParsingContext {
    public Environment environment;
    ParsingContext parent;
    public Map<String, LocalVariable> variables = new TreeMap<>();
    int[] nextIndex;

    /**
     * Set at closure boundaries. Interleaved: original index, local index.
     */
    Closure closure;


    public ParsingContext(Environment environment) {
        this.environment = environment;
        nextIndex = new int[1];
    }

    public ParsingContext(ParsingContext parent, boolean closureBoundary) {
        this(parent.environment);
        this.parent = parent;
        if (closureBoundary) {
            this.closure = new Closure();
            this.nextIndex = new int[1];
        } else {
            this.nextIndex = parent.nextIndex;
        }
    }

    public LocalVariable resolve(String name) {
        LocalVariable result = variables.get(name);
        if (result != null) {
            return result;
        }
        if (parent == null) {
            return null;
        }
        result = parent.resolve(name);
        if (closure != null && result != null) {
            int originalIndex = result.getIndex();
            result = addVariable(name, result.getType());
            closure.addMapping(name, originalIndex, result.getIndex());
        }
        return result;
    }

    public LocalVariable addVariable(String name, Type type) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Local variable '" + name + "' already defined.");
        }
        LocalVariable variable = new LocalVariable(name, type, nextIndex[0]++);
        variables.put(name, variable);
        return variable;
    }

    public int getVarCount() {
        return nextIndex[0];
    }

    public Closure getClosure() {
        closure.setVarCount(getVarCount());
        return closure;
    }

    public EvaluationContext createEvaluationContext() {
        if (parent != null) {
            throw new RuntimeException("Can create evaluationContext only for root parsing context");
        }
        return new EvaluationContext(environment, getVarCount());
    }
}