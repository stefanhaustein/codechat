package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Type;

public class Literal extends Expression {
    public final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.of(value);
    }

    @Override
    public int getPrecedence() {
        return value instanceof Number && ((Number) value).doubleValue() < 0 ? Parser.PRECEDENCE_SIGN : Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        Formatting.toLiteral(sb, value);
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return value;
    }

    @Override
    public Literal reconstruct(Expression... children) {
        return new Literal(value);
    }
}
