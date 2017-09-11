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
        PENDING, STUB_SERIALIZED, FULLY_SERIALIZED
    }

    private Map<Entity, SerializationState> stateMap = new HashMap<>();
    // Map<Entity, String> nameMap = new HashMap<>();

    public SerializationState getState(Entity value) {
        return stateMap.get(value);
    }


    public void setState(Entity entity, SerializationState state) {
        stateMap.put(entity, state);
    }
}
