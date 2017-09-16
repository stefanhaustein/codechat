package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.FunctionType;

public class Assignment extends AbstractStatement {
    public Expression left;
    public Expression right;

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
    public void toString(StringBuilder sb, int indent) {
        if (left instanceof RootVariableNode) {
            RootVariable variable = ((RootVariableNode) left).rootVariable;
            if (variable.constant && variable.type instanceof FunctionType && right instanceof FunctionExpression) {
                ((FunctionExpression) right).toString(sb, indent, variable.name);
                return;
            }
        }
        indent(sb, indent);
        left.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append(";\n");

    }

    @Override
    public void getDependencies(Environment environment, Collection<Entity> result) {
        left.getDependencies(environment, result);
        right.getDependencies(environment, result);
    }
}
