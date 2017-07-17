package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;

public class CountStatement extends AbstractStatement {
    LocalVariable variable;
    Expression expression;
    Statement body;

    public CountStatement(LocalVariable counter, Expression expression, Statement body) {
        this.variable = counter;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        double limit = ((Number) expression.eval(context)).doubleValue();
        for (double i = 0; i < limit; i++) {
            context.variables[variable.getIndex()] = i;
            body.eval(context);
        }

        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        indent(sb, indent);
        sb.append("count ").append(variable.getName()).append(" to ");
        expression.toString(sb, 0);
        sb.append(":\n");

        body.toString(sb, indent + 1);

        indent(sb, indent);
        sb.append("end;\n");
    }
}
