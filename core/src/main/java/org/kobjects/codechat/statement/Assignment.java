package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;

public class Assignment extends AbstractStatement {
    private final Expression left;
    private final Expression right;

    public Assignment(Expression left, Expression right) {
        if (!left.isAssignable()) {
            throw new RuntimeException("Not assignable: " + left);
        }

        if (!left.getType().equals(right.getType())) {
            throw new RuntimeException("Incompatible types for assignment: " + this);
        }
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Object value = right.eval(context);
        left.assign(context, value);
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
      sb.indent(indent);
      left.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append("\n");

    }

    @Override
    public void getDependencies(DependencyCollector result) {
        left.getDependencies(result);
        right.getDependencies(result);
    }
}
