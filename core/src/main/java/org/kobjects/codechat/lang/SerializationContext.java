package org.kobjects.codechat.lang;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class SerializationContext {
    public enum Mode {SAVE, EDIT, LIST, SAVE2}

    private final HashSet<Entity> serialized = new HashSet<>();
    private final LinkedHashSet<Entity> queue = new LinkedHashSet<>();
    private final LinkedHashSet<Entity> queue2 = new LinkedHashSet<>();
    private final Environment environment;
    private Mode mode;

    public SerializationContext(Environment environment, Mode mode) {
        this.environment = environment;
        this.mode = mode;
    }

    public boolean isSerialized(Entity entity) {
        return serialized.contains(entity);
    }

    public void setSerialized(Entity entity) {
        serialized.add(entity);
        queue.remove(entity);
    }

    public void setNeedsPhase2(Entity entity) {
        if (mode != Mode.SAVE) {
            throw new IllegalStateException("Phase two can only be requested in mode SAVE.");
        }
        setSerialized(entity);
        queue2.add(entity);
    }

    public void enqueue(Entity entity) {
        if (!serialized.contains(entity) && !queue.contains(entity)) {
            queue.add(entity);
        }
    }

    public Entity pollPending() {
        if (queue.isEmpty()) {
            if (mode == Mode.SAVE) {
                queue.addAll(queue2);
                queue2.clear();
                mode = Mode.SAVE2;
                return pollPending();
            }
            return null;
        }
        Entity result = queue.iterator().next();

        // Enrichment This is here so "on..." gets resolved last as it depends on.
        // The problems is that parts of the trigger expressions need to get resolved immmediately.
        // A cleaner solution would be to postpone the initialization part to the end in loading somehow,
        // as on expressions can be assigned to variables, and in this case this code does not help.
        if (result instanceof HasDependencies) {
            HasDependencies hasDependencies = (HasDependencies) result;
            DependencyCollector dependencyCollector = new DependencyCollector(environment);
            hasDependencies.getDependencies(dependencyCollector);
            for (Entity dep: dependencyCollector.get()) {
                enqueue(dep);
            }
        }
        queue.remove(result);
        return result;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public Mode getMode() {
        return mode;
    }

}
