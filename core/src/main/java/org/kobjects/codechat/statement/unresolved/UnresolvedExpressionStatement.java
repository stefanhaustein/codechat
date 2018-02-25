package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.InvocationExpr;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UnresolvedExpressionStatement extends  UnresolvedStatement {
    private final UnresolvedExpression expression;
    private final boolean interactive;

    public UnresolvedExpressionStatement(UnresolvedExpression expression) {
        this(expression, false);
    }

    public UnresolvedExpressionStatement(UnresolvedExpression expression, boolean interactive) {
        this.expression = expression;
        this.interactive = interactive;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      expression.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public Statement resolve(ParsingContext parsingContext) {
        Expression resolved = expression.resolve(parsingContext, null);
        if (resolved.getType() instanceof FunctionType) {
           return new ExpressionStatement(new InvocationExpr(resolved, false));
        }
        return new ExpressionStatement(resolved);
    }
}
