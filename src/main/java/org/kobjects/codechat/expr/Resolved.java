package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Scope;

public abstract class Resolved extends Expression {

    public final Expression resolve(Scope scope) {
        throw new RuntimeException("Already resolved: " + toString());
    }

}
