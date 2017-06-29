package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.Type;

public class BinaryLogicalOperator extends AbstractBinaryOperator {

    public BinaryLogicalOperator(char name, Expression left, Expression right) {
        super(name, left, right);
        if (left.getType() != Type.BOOLEAN|| right.getType() != Type.BOOLEAN) {
            throw new IllegalArgumentException("Arguments of '" + name + "' must be of type Boolean.");
        }
    }

    @Override
    public Object eval(EvaluationContext context) {
        switch(name) {
            case '\u2227':
                return Boolean.TRUE.equals(left.eval(context)) ? right.eval(context) : Boolean.FALSE;
            case '\u2228':
                return Boolean.FALSE.equals(left.eval(context)) ? right.eval(context) : Boolean.TRUE;
            default:
                throw new RuntimeException("Impossible");
        }
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

}
