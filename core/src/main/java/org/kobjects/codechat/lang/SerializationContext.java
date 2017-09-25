package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public class SerializationContext {

    public enum SerializationState {
        UNVISITED,             //
        STUB_SERIALIZED,       // A stub was serialized
        FULLY_SERIALIZED       // Fully serialized
    }

    private final SerializationState defaultState;
    private final Map<Entity, SerializationState> stateMap = new HashMap<>();
    private final LinkedHashSet<Entity> queue = new LinkedHashSet<>();
    private final Environment environment;

    public SerializationContext(Environment environment) {
        this(environment, SerializationState.UNVISITED);
    }

    public SerializationContext(Environment environment, SerializationState defaultState) {
        this.environment = environment;
        this.defaultState = defaultState;
    }

    // Map<Entity, String> nameMap = new HashMap<>();

    public SerializationState getState(Entity value) {
        SerializationState state = stateMap.get(value);
        return state == null ? defaultState : state;
    }


    public void setState(Entity entity, SerializationState state) {
        stateMap.put(entity, state);
        queue.remove(entity);
    }

    public void enqueue(Entity entity) {
        if (!stateMap.containsKey(entity)) {
            queue.add(entity);
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

    public void serializeDependencies(AnnotatedStringBuilder asb, HasDependencies hasDependencies) {
        DependencyCollector dependencyCollector = new DependencyCollector(environment);
        hasDependencies.getDependencies(dependencyCollector);
        for (Entity entity: dependencyCollector.getStrong()) {
            if (getState(entity) == SerializationContext.SerializationState.UNVISITED
                    && (!(entity instanceof RootVariable) || !((RootVariable) entity).builtin)) {
                entity.serializeStub(asb);
                setState(entity, SerializationContext.SerializationState.STUB_SERIALIZED);
            }
        }
        for (Entity entity: dependencyCollector.getWeak()) {
            enqueue(entity);
        }

    }

}
