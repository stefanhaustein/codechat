package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Evaluable;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public abstract class Expression implements Evaluable {

    public void assign(Context context, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public abstract Expression resolve(Scope scope);

    public abstract Type getType();

    public abstract void toString(StringBuilder sb, int parentPrecedence);
}
