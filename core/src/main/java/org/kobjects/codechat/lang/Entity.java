package org.kobjects.codechat.lang;

import java.util.Map;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;

/**
 * Shared interface for variables and instances that have an identity.
 * Defines a serialization entry point. Data inside is usually serialized in a way that
 */
public interface Entity {
    void serializeStub(AnnotatedStringBuilder asb, SerializationContext serializationContext);
    void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext);

    void setUnparsed(String unparsed);

    String getUnparsed();
}
