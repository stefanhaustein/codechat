package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;

public class ReturnStatement extends AbstractStatement {

    public Expression expression;
    public ReturnStatement(Expression expression) {
        this.expression = expression;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return expression.eval(context);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("return ");
        expression.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(Environment environment, DependencyCollector result) {
        expression.getDependencies(environment, result);
    }
}
