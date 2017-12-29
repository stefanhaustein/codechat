package org.kobjects.codechat.lang;

import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.Typed;

/**
 * Shared interface for variables and instances that have an identity.
 * Defines a serialization entry point. Data inside is usually serialized in a way that
 */
public interface Entity extends Typed {
    void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext);
    void delete();
}
