package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Parser;

public class Literal extends Node {
    final Object value;

    public Literal(Object value) {
        this.value = value;
    }

    public Object eval(Environment environment) {
        return value;
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

}
