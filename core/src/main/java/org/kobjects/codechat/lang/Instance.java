package org.kobjects.codechat.lang;

import java.util.List;

public interface Instance {

    int getId();
    void serializeDeclaration(StringBuilder sb, List<Annotation> annotations);
    void serializeDefinition(StringBuilder sb, boolean all);
}
