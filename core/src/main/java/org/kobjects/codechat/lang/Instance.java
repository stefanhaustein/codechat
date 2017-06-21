package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.type.Typed;

/**
 * Note that instances are not necessarily (constructor-) instantiable
 */
public interface Instance extends Typed {
    int getId();
    void serializeDeclaration(StringBuilder sb, List<Annotation> annotations);
    void serializeDefinition(StringBuilder sb, boolean all);
    void delete();
}
