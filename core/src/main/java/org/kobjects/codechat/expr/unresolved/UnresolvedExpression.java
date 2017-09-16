package org.kobjects.codechat.expr.unresolved;


import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;

public abstract class UnresolvedExpression {

    final int start;
    final int end;

    public UnresolvedExpression(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public abstract Expression resolve(ParsingContext parsingContext, Type expectedType);

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
