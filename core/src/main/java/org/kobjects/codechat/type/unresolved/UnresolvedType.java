package org.kobjects.codechat.type.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;

public interface UnresolvedType {

    Type resolve(ParsingContext parsingContext);

    Type resolve(Environment environment);

    void print(AnnotatedStringBuilder asb);
}
