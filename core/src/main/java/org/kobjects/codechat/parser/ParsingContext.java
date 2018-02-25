package org.kobjects.codechat.parser;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.UserClassifier;

public class ParsingContext {
    public enum Mode {
        INTERACTIVE, SYNTAX_CHECK, LOAD
    }

    public ParsingEnvironment environment;
    public Queue<Runnable> queue;
    ParsingContext parent;
    public Map<String, LocalVariable> variables = new TreeMap<>();
    int[] nextIndex;
    public final Mode mode;
    public final UserClassifier classType;

    /**
     * Set at closure boundaries. Interleaved: original index, local index.
     */
    Closure closure;


    public ParsingContext(Environment environment, Mode mode) {
        this.environment = mode == Mode.SYNTAX_CHECK ? new FakeEnvironment(environment) : environment;
        this.mode = mode;
        nextIndex = new int[1];
        classType = null;
        queue = new ArrayDeque<>();
    }

    public ParsingContext(ParsingContext parent, boolean closureBoundary) {
        this.environment = parent.environment;
        this.mode = parent.mode;
        this.queue = parent.queue;
        this.parent = parent;
        if (closureBoundary) {
            this.closure = new Closure();
            this.nextIndex = new int[1];
            this.classType = null;
        } else {
            this.nextIndex = parent.nextIndex;
            this.classType = parent.classType;
        }
    }

    public ParsingContext(ParsingContext parent, UserClassifier classType) {
        this.environment = parent.environment;
        this.mode = parent.mode;
        this.parent = parent;
        this.closure = new Closure();
        this.nextIndex = new int[1];
        this.classType = classType;
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
            result = addVariable(name, result.getType(), true);
            closure.addMapping(name, originalIndex, result.getIndex());
        }
        return result;
    }

    public void enqueue(Runnable runnable) {
        queue.add(runnable);
    }

    public LocalVariable addVariable(String name, Type type, boolean constant) {
        if (variables.containsKey(name)) {
            throw new RuntimeException("Local variable '" + name + "' already defined.");
        }
        LocalVariable variable = new LocalVariable(name, type, nextIndex[0]++, constant);
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

    public EvaluationContext createEvaluationContext(Environment environment) {
        if (parent != null) {
            throw new RuntimeException("Can create evaluationContext only for root parsing context");
        }
        return new EvaluationContext(environment, getVarCount());
    }

    public void resolveQueued() {
        while (!queue.isEmpty()) {
            queue.poll().run();
        }
    }

}
