package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.type.Typed;

/**
 * Note that instances are not necessarily (constructor-) instantiable
 */
public interface Instance extends Typed {

    enum Detail {DECLARATION, DEFINITION, FULL}

    int getId();
    void serialize(StringBuilder sb, Detail detail, List<Annotation> annotations);
    void delete();
}
