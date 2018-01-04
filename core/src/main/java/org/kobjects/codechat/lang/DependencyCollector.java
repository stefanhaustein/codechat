package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;

public class DependencyCollector {

    private final LinkedHashSet<Entity> dependencies = new LinkedHashSet<>();

    public void add(Instance instance) {
        dependencies.add(instance);
    }

    public void addVariable(RootVariable variable) {
        dependencies.add(variable);
    }


    public Iterable<Entity> get() {
        return dependencies;
    }


    public boolean contains(Entity entity) {
        return dependencies.contains(entity);
    }
}
