package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public class SerializationContext {

    public enum Detail {
        DECLARATION, // new Foo#31
        DEFINITION,  // Foo#31{bar: "x"}
        DETAIL       // Foo#31.bar = "x"
    }

    public enum SerializationState {
        UNVISITED,             //
        PENDING,               // Serialization started & currently serializing dependencies
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
        DependencyCollector dependencyCollector = new DependencyCollector();
        hasDependencies.getDependencies(getEnvironment(), dependencyCollector);
        for (Entity entity: dependencyCollector.getStrong()) {
            if (getState(entity) == SerializationContext.SerializationState.UNVISITED) {
                entity.serializeStub(asb);
                setState(entity, SerializationContext.SerializationState.STUB_SERIALIZED);
            }
        }
        for (Entity entity: dependencyCollector.getWeak()) {
            enqueue(entity);
        }

    }

}
