package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public abstract class Expression {

    public abstract Object eval(Context context);

    public void assign(Context context, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public abstract Expression resolve(Scope scope);

    public abstract Type getType();

    public abstract int getPrecedence();

    public abstract void toString(StringBuilder sb);

    public final void toString(StringBuilder sb, int parentPrecedence) {
        if (parentPrecedence > getPrecedence()) {
            sb.append('(');
            toString(sb);
            sb.append(')');
        } else {
            toString(sb);
        }
    }

    public abstract int getChildCount();

    public Expression getChild(int i) {
        throw new UnsupportedOperationException();
    }

    public boolean isAssignable() {
        return false;
    }
}
