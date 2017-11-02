package org.kobjects.codechat.lang;

import java.util.HashSet;
import java.util.LinkedHashSet;

public class SerializationContext {
    public enum Mode {SAVE, EDIT}

    private final HashSet<Entity> serialized = new HashSet<>();
    private final LinkedHashSet<Entity> queue = new LinkedHashSet<>();
    private final Environment environment;
    private final Mode mode;

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

    public void enqueue(Entity entity) {
        if (!serialized.contains(entity) && !queue.contains(entity)) {
            queue.add(entity);

            if (entity instanceof HasDependencies) {
                HasDependencies hasDependencies = (HasDependencies) entity;
                DependencyCollector dependencyCollector = new DependencyCollector(environment);
                hasDependencies.getDependencies(dependencyCollector);
                for (Entity dep: dependencyCollector.get()) {
                    enqueue(dep);
                }
            }
        }
    }

    public Entity pollPending() {
        if (queue.isEmpty()) {
            return null;
        }
        Entity result = queue.iterator().next();
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
