package org.kobjects.codechat.expr.unresolved;


import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.ParsingContext;

public abstract class UnresolvedExpression {

    public abstract Expression resolve(ParsingContext parsingContext);

    public abstract void toString(StringBuilder sb, int indent);

    public abstract int getPrecedence();

    public final void toString(StringBuilder sb, int indent, int parentPrecedence) {
        if (parentPrecedence > getPrecedence()) {
             sb.append('(');
             toString(sb, indent);
             sb.append(')');
        } else {
            toString(sb, indent);
        }
    }

    public final String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0, 0);
        return sb.toString();
    }
}
