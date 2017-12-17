package org.kobjects.codechat.expr.unresolved;


import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;

public abstract class UnresolvedExpression {

    public final int start;
    public final int end;

    public UnresolvedExpression(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public abstract Expression resolve(ParsingContext parsingContext, Type expectedType);

    public abstract void toString(AnnotatedStringBuilder sb, int indent);

    public abstract int getPrecedence();

    public final void toString(AnnotatedStringBuilder asb, int indent, int parentPrecedence) {
        if (parentPrecedence > getPrecedence()) {
             asb.append('(');
             toString(asb, indent);
             asb.append(')');
        } else {
            toString(asb, indent);
        }
    }

    public final String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        toString(asb, 0, 0);
        return asb.toString();
    }
}
