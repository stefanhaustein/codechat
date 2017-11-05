package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.Type;

public class BinaryMathOperator extends AbstractBinaryOperator {

    public BinaryMathOperator(char name, Expression left, Expression right) {
        super(name, left, right);
        if (left.getType() != Type.NUMBER || right.getType() != Type.NUMBER) {
            throw new IllegalArgumentException("Argumnets of '" + name + "' must be of type Number.");
        }
    }

    @Override
    public Object eval(EvaluationContext context) {
        double l = ((Number) left.eval(context)).doubleValue();
        double r = ((Number) right.eval(context)).doubleValue();

        switch (name) {
            case '+': return l + r;
            case '-': return l - r;
            case '\u00d7': return l * r;
            case '/': return l / r;
            case '^': return Math.pow(l, r);
            case '\u221a': return l == 2 ? Math.sqrt(r) : Math.pow(r, 1/l);
            default:
                throw new RuntimeException("Impossible");
        }
    }

    @Override
    public Type getType() {
        return left.getType();
    }


    @Override
    public BinaryMathOperator reconstruct(Expression[] children) {
        return new BinaryMathOperator(name, children[0], children[1]);
    }
}
