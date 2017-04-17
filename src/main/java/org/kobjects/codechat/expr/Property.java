package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Parser;

public class Property extends Node {
    String name;
    Node base;

    public Property(Node left, Node right) {
        if (!(right instanceof Identifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((Identifier) right).name;
    }

    @Override
    public Object eval(Environment environment) {
        Object value = base.eval(environment);
        try {
            Method method = value.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return method.invoke(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(Environment environment, Object value) {
        Object baseValue = base.eval(environment);
        try {
            Class c = value.getClass();
            Method method = baseValue.getClass().getMethod("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1),
                    c == Double.class ? Double.TYPE : c);
            method.invoke(baseValue, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void toString(StringBuilder sb, int parentPrecedence) {
        boolean braces = parentPrecedence > Parser.PRECEDENCE_PATH;
        if (braces) {
            sb.append('(');
        }
        base.toString(sb, Parser.PRECEDENCE_PATH);
        sb.append('.');
        sb.append(name);
        if (braces) {
            sb.append(')');
        }
    }
}
