package org.kobjects.codechat.statement;

import java.util.ArrayList;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;

public class ForeachStatement extends AbstractStatement {
    LocalVariable variable;
    Expression expression;
    Statement body;

    public ForeachStatement(LocalVariable var, Expression expression, Statement body) {
        this.variable = var;
        this.expression = expression;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Iterable iterable = ((Iterable) expression.eval(context));
        ArrayList defensiveCopy = new ArrayList();
        synchronized (iterable) {
            for (Object i : iterable) {
                defensiveCopy.add(i);
            }
        }
        for (Object o : defensiveCopy) {
            context.variables[variable.getIndex()] = o;
            body.eval(context);
        }

        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        indent(sb, indent);
        sb.append("foreach ").append(variable.getName()).append(' ');
        expression.toString(sb, 0);
        sb.append(" {\n");

        body.toString(sb, indent + 1);

        indent(sb, indent);
        sb.append("}\n");
    }
}
