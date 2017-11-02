package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class DependencyCollector {

    private final Environment environment;
    private final LinkedHashSet<Entity> dependencies = new LinkedHashSet<>();

    public DependencyCollector(Environment environment) {
        this.environment = environment;
    }

    public void add(Entity entity) {
        dependencies.add(entity);
    }

    public Iterable<Entity> get() {
        return dependencies;
    }


    public Environment getEnvironment() {
        return environment;
    }

    public boolean contains(Entity entity) {
        return dependencies.contains(entity);
    }
}
