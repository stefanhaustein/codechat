package org.kobjects.codechat.statement;

import java.util.ArrayList;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.type.CollectionType;

public class ForStatement extends AbstractStatement {
    LocalVariable variable;
    Expression expression;
    Statement body;

    public ForStatement(LocalVariable var, Expression expression, Statement body) {
        if (!(expression.getType() instanceof CollectionType)) {
            throw new RuntimeException("For expression must be a list.");
        }
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
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.indent(indent);
        sb.append("for ").append(variable.getName()).append(" in ");
        expression.toString(sb, 0);
        sb.append(":\n");

        body.toString(sb, indent + 2);

        sb.indent(indent);
        sb.append("end\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expression.getDependencies(result);
        body.getDependencies(result);
    }
}
