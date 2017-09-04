package org.kobjects.codechat.lang;

import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

public interface Dependency {
    void serialize(AnnotatedStringBuilder asb, Instance.Detail detail, Map<Dependency, Environment.SerializationState> serializationStateMap);
}
