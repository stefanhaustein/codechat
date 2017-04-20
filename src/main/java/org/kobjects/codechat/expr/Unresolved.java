package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Type;

public abstract class Unresolved extends Expression {

    @Override
    public final Type getType() {
        throw new RuntimeException("Unresolved: " + toString());
    }

    @Override
    public final Object eval(Context context) {
        throw new RuntimeException("Unresolved: " + toString());
    }
}
