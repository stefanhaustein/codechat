package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Context;

public class ExpressionStatement extends AbstractStatement {

    public Expression expression;
    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object eval(Context context) {
        expression.eval(context);
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        expression.toString(sb);
        sb.append(";\n");
    }
}
