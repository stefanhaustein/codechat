package org.kobjects.codechat.lang;

import java.util.HashSet;
import java.util.LinkedHashSet;


public class SerializationContext {
    private final HashSet<Instance> serialized = new HashSet<>();
    private final LinkedHashSet<Instance> queue = new LinkedHashSet<>();
    private final LinkedHashSet<Instance> queue2 = new LinkedHashSet<>();
    private Printable.Flavor mode;

    public SerializationContext(Printable.Flavor mode) {
        this.mode = mode;
    }

    public boolean isSerialized(Instance instance) {
        return serialized.contains(instance);
    }

    public void setSerialized(Instance instance) {
        serialized.add(instance);
        queue.remove(instance);

        if (mode == Printable.Flavor.SAVE && instance.needsTwoPhaseSerilaization()) {
            queue2.add(instance);
        }
        if (mode != Printable.Flavor.SAVE2) {
            DependencyCollector dependencyCollector = new DependencyCollector(instance.getEnvironment());
            instance.getDependencies(dependencyCollector);
            for (Instance dep : dependencyCollector.get()) {
               enqueue(dep);
            }
        }
    }

    public void enqueue(Instance entity) {
        if (!serialized.contains(entity) && !queue.contains(entity)) {
            queue.add(entity);
        }
    }

    public Instance pollPending() {
        if (queue.isEmpty()) {
            if (mode == Printable.Flavor.SAVE) {
                queue.addAll(queue2);
                queue2.clear();
                mode = Printable.Flavor.SAVE2;
                return pollPending();
            }
            return null;
        }
        Instance result = queue.iterator().next();

        // Enrichment This is here so "on..." gets resolved last as it depends on.
        // The problems is that parts of the trigger expressions need to get resolved immmediately.
        // A cleaner solution would be to postpone the initialization part to the end in loading somehow,
        // as on expressions can be assigned to variables, and in this case this code does not help.

        setSerialized(result);

        return result;
    }

    public Printable.Flavor getMode() {
        return mode;
    }

}
