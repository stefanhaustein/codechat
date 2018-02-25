package org.kobjects.codechat.lang;

import java.util.LinkedHashSet;
import org.kobjects.codechat.instance.Instance;

public class DependencyCollector {

    private final LinkedHashSet<Instance> instances = new LinkedHashSet<>();
    private final LinkedHashSet<RootVariable> variables = new LinkedHashSet<>();
    private final Environment environment;

    DependencyCollector(Environment environment) {
        this.environment = environment;
    }
    
    public void add(Instance instance) {
        instances.add(instance);
    }

    public void addVariable(RootVariable variable) {
        variables.add(variable);
    }


    public Iterable<Instance> get() {
        return instances;
    }
    public Iterable<RootVariable> getVariables() {
        return variables;
    }


    public boolean contains(Instance instance) {
        return instances.contains(instance);
    }

    public boolean contains(RootVariable variable) {
        return variables.contains(variable);
    }

    public Environment getEnvironment() {
        return environment;
    }
}
