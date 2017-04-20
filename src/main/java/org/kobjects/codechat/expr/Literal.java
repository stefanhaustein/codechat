package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
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
        if (value instanceof Number) {
            Number n = (Number) value;
            if (n.longValue() == n.doubleValue()) {
                sb.append(n.longValue());
            } else {
                sb.append(n.doubleValue());
            }
        } else if (value instanceof String) {
            sb.append(Environment.quote((String) value));
        } else {
            sb.append(value);
        }
    }

    @Override
    public Object eval(Context context) {
        return value;
    }
}
