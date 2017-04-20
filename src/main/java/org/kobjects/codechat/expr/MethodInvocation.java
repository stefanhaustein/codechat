package org.kobjects.codechat.expr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class MethodInvocation extends Expression {
    static final Object[] EMPTY_ARRAY = new Object[0];

    Method method;
    Type type;
    Expression[] children;

    MethodInvocation(Method method, Expression[] children) {
        this.method = method;
        this.type = Type.forJavaClass(method.getReturnType());
        this.children = children;
    }

    @Override
    public Object eval(Context context) {
        Object base = children[0].eval(context);
        Object[] param;
        if (children.length <= 1) {
            param = EMPTY_ARRAY;
        } else {
            param = new Object[children.length - 1];
            for (int i = 0; i < param.length; i++) {
                param[i] = children[i + 1].eval(context);
            }
        }
        try {
            return method.invoke(base, param);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Expression resolve(Scope scope) {
        throw new RuntimeException("Already resolved");
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {

    }
}
