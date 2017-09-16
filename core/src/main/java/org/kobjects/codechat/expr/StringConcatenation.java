package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.Type;

public class StringConcatenation extends AbstractBinaryOperator {

    public StringConcatenation(Expression left, Expression right) {
        super('+', left, right);
    }

    @Override
    public Object eval(EvaluationContext context) {
        return String.valueOf(left.eval(context)) + String.valueOf(right.eval(context));
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

}
