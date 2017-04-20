package org.kobjects.codechat.expr;

import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class Property extends Expression {
    String name;
    Expression base;

    public Property(Expression left, Expression right) {
        if (!(right instanceof Identifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((Identifier) right).name;
    }

    @Override
    public Object eval(Context context) {
        Object value = base.eval(context);
        try {
            Method method = value.getClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return method.invoke(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(Context context, Object value) {
        Object baseValue = base.eval(context);
        try {
            Class c = value.getClass();
            Method method = baseValue.getClass().getMethod("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1),
                    c == Double.class ? Double.TYPE : c);
            method.invoke(baseValue, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Expression resolve(Scope scope) {
        base = base.resolve(scope);
        return this;
    }

    @Override
    public Type getType() {
        try {
            Method method = base.getType().getJavaClass().getMethod("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
            return Type.forJavaClass(method.getReturnType());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(StringBuilder sb) {
        base.toString(sb, Parser.PRECEDENCE_PATH);
        sb.append('.');
        sb.append(name);
    }
}
