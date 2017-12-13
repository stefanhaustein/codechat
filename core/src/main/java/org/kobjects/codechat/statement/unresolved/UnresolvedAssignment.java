package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Assignment;
import org.kobjects.codechat.statement.Statement;

public class UnresolvedAssignment extends UnresolvedStatement {
    UnresolvedExpression left;
    UnresolvedExpression right;

    public UnresolvedAssignment(UnresolvedExpression left, UnresolvedExpression right) {
        this.left = left;
        this.right = right;
    }


    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        left.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
    }

    @Override
    public Assignment resolve(ParsingContext parsingContext) {
        Expression resolvedTarget = left.resolve(parsingContext, null);
        return new Assignment(resolvedTarget, right.resolve(parsingContext, resolvedTarget.getType()));
    }
}
