package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.ParsingContext;

public abstract class AbstractResolved extends Expression {

    public final Expression resolve(ParsingContext parsingContext) {
        throw new RuntimeException("Already resolved: " + toString());
    }

}
