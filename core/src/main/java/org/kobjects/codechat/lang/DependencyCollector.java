package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DependencyCollector {

    public enum Type {
        WEAK,
        STRONG
    }

    private final Environment environment;
    private final HashMap<Entity,Type> dependencies = new HashMap<>();

    public DependencyCollector(Environment environment) {
        this.environment = environment;
    }

    public void addStrong(Entity strongDependency) {
        dependencies.put(strongDependency, Type.STRONG);
    }

    public void addWeak(Entity weakDependency) {
        if (!dependencies.containsKey(weakDependency)) {
            dependencies.put(weakDependency, Type.WEAK);
        }
    }

    public Iterable<Entity> getStrong() {
        HashSet result = new HashSet();
        for (Map.Entry<Entity,Type> entry : dependencies.entrySet()) {
            if (entry.getValue() == Type.STRONG) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Iterable<Entity> getWeak() {
        HashSet result = new HashSet();
        for (Map.Entry<Entity,Type> entry : dependencies.entrySet()) {
            if (entry.getValue() == Type.WEAK) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public boolean contains(Entity entity) {
        return dependencies.containsKey(entity);
    }
}
