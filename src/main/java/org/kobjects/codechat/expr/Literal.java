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
    public void toString(StringBuilder sb, int parentPrecedence) {
        if (value instanceof Number) {
            Number n = (Number) value;
            boolean brackets = n.doubleValue() < 0 && parentPrecedence > Parser.PRECEDENCE_SIGN;
            if (brackets) {
                sb.append('(');
            }
            if (n.longValue() == n.doubleValue()) {
                sb.append(n.longValue());
            } else {
                sb.append(n.doubleValue());
            }
            if (brackets) {
                sb.append(')');
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
