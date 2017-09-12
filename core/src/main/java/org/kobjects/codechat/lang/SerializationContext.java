package org.kobjects.codechat.lang;

import java.util.HashMap;
import java.util.Map;

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

    public SerializationContext() {
        this(SerializationState.UNVISITED);
    }

    public SerializationContext(SerializationState defaultState) {
        this.defaultState = defaultState;
    }

    private Map<Entity, SerializationState> stateMap = new HashMap<>();
    // Map<Entity, String> nameMap = new HashMap<>();

    public SerializationState getState(Entity value) {
        SerializationState state = stateMap.get(value);
        return state == null ? defaultState : state;
    }


    public void setState(Entity entity, SerializationState state) {
        stateMap.put(entity, state);
    }
}
