package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Variable;

public class CountStatement extends AbstractStatement {
    Variable variable;
    Expression expression;
    Block body;

    public CountStatement(Variable counter, Expression expression, Block body) {
        this.variable = counter;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public Object eval(Context context) {
        double limit = ((Number) expression.eval(context)).doubleValue();

        for (double i = 0; i < limit; i++) {
            context.variables[variable.getIndex()] = i;
            body.eval(context);
        }

        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        indent(sb, indent);
        sb.append("count ").append(variable.getName()).append(' ');
        expression.toString(sb);
        sb.append(" {\n");

        body.toString(sb, indent + 1);

        indent(sb, indent);
        sb.append("}\n");
    }
}
