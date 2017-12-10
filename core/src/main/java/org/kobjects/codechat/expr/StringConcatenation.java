package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.type.Type;

public class StringConcatenation extends AbstractBinaryOperator {

    public StringConcatenation(Expression left, Expression right) {
        super('+', left, right);
    }

    @Override
    public Object eval(EvaluationContext context) {
        return Formatting.toString(left.eval(context)) + Formatting.toString(right.eval(context));
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public Expression reconstruct(Expression... children) {
        return new StringConcatenation(children[0], children[1]);
    }

}
