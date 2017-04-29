package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class Literal extends Expression {
    final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    @Override
    public Expression resolve(Scope scope) {
        return this;
    }

    @Override
    public Type getType() {
        return Type.forJavaClass(value.getClass());
    }

    @Override
    public int getPrecedence() {
        return value instanceof Number && ((Number) value).doubleValue() < 0 ? Parser.PRECEDENCE_SIGN : Parser.PRECEDENCE_PATH;
    }


    @Override
    public void toString(StringBuilder sb) {
        sb.append(Formatting.toLiteral(value));
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public Object eval(Context context) {
        return value;
    }
}
