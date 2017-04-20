package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Type;

public class Assignment extends Resolved {
    Expression left;
    Expression right;

    public Assignment(Expression left, Expression right) {
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
    public Type getType() {
        return right.getType();
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        boolean parens = parentPrecedence > Parser.PRECEDENCE_EQUALITY;
        if (parens) {
            sb.append('(');
        }
        left.toString(sb, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, Parser.PRECEDENCE_EQUALITY);
        if (parens) {
            sb.append(')');
        }
    }
}
