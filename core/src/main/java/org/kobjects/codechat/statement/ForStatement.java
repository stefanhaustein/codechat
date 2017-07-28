package org.kobjects.codechat.statement;

import java.util.ArrayList;
import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;

public class ForStatement extends AbstractStatement {
    LocalVariable variable;
    Expression expression;
    Statement body;

    public ForStatement(LocalVariable var, Expression expression, Statement body) {
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
        sb.append("for ").append(variable.getName()).append(" in ");
        expression.toString(sb, 0);
        sb.append(":\n");

        body.toString(sb, indent + 1);

        indent(sb, indent);
        sb.append("end;\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        expression.getDependencies(environment, result);
        body.getDependencies(environment, result);
    }
}
