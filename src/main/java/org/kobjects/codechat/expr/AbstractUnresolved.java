package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Type;

public abstract class AbstractUnresolved extends Expression {

    @Override
    public final Type getType() {
        throw new RuntimeException("Unresolved: " + toString());
    }

    @Override
    public final Object eval(EvaluationContext context) {
        throw new RuntimeException("Unresolved: " + toString());
    }
}
