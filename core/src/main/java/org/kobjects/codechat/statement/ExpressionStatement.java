package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;

public class ExpressionStatement extends AbstractStatement {

    public Expression expression;
    public ExpressionStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object eval(EvaluationContext context) {
        expression.eval(context);
        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        expression.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        expression.getDependencies(environment, result);
    }
}
