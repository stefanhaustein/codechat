package org.kobjects.codechat.type.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;

public class UnresolvedNamedType implements UnresolvedType {
    private final String name;

    public UnresolvedNamedType(String name) {
        this.name = name;
    }


    @Override
    public Type resolve(ParsingContext parsingContext) {
        return resolve(parsingContext.environment.getEnvironment());
    }

    @Override
    public Type resolve(Environment environment) {
        return environment.resolveType(name);
    }

    @Override
    public void print(AnnotatedStringBuilder asb) {
        asb.append(name);
    }
}
