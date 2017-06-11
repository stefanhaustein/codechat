package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public abstract class Expression {

    public abstract Object eval(EvaluationContext context);

    public void assign(EvaluationContext context, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0, 0);
        return sb.toString();
    }

    public abstract Expression resolve(ParsingContext parsingContext);

    public abstract Type getType();

    public abstract int getPrecedence();

    public abstract void toString(StringBuilder sb, int indent);

    public final void toString(StringBuilder sb, int indent, int parentPrecedence) {
        if (parentPrecedence > getPrecedence()) {
            sb.append('(');
            toString(sb, indent);
            sb.append(')');
        } else {
            toString(sb, indent);
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
