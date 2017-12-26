package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
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
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      expression.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expression.getDependencies(result);
    }
}
