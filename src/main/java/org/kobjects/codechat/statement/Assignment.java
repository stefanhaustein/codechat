package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

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
    public Object eval(Context context) {
        Object value = right.eval(context);
        left.assign(context, value);
        return value;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        indent(sb, indent);
        left.toString(sb, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, Parser.PRECEDENCE_EQUALITY);

    }

}
