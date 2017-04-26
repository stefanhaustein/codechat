package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class Assignment extends Expression {
    public Expression left;
    public Expression right;

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
    public Expression resolve(Scope scope) {
        left = left.resolve(scope);
        right = right.resolve(scope);

        if (!left.isAssignable()) {
            throw new RuntimeException("Not assignable: " + left);
        }

        if (!left.getType().equals(right.getType())) {
            throw new RuntimeException("Incompatible types for assignment: " + this);
        }

        return this;
    }

    @Override
    public Type getType() {
        return right.getType();
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_EQUALITY;
    }

    @Override
    public void toString(StringBuilder sb) {
        left.toString(sb, Parser.PRECEDENCE_EQUALITY);
        sb.append(" = ");
        right.toString(sb, Parser.PRECEDENCE_EQUALITY);
    }
}
