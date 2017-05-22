package org.kobjects.codechat.expr;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Type;

public class BuiltinInvocation extends AbstractResolved {
    static final Object[] EMPTY_ARRAY = new Object[0];

    boolean parens;
    Method method;
    Expression[] children;
    Type type;

    public BuiltinInvocation(Method method, boolean parens, Expression... children) {
        this.type = Type.forJavaType(method.getGenericReturnType());
        this.method = method;
        this.parens = parens;
        this.children = children;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Object[] params;
        if (children.length == 0) {
            params = EMPTY_ARRAY;
        } else {
            params = new Object[children.length];
            for (int i = 0; i < children.length; i++) {
                params[i] = children[i].eval(context);
            }
        }
        try {
            return method.invoke(context.environment.builtins, params);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return UnresolvedInvocation.getPrecedence(parens);
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        UnresolvedInvocation.toString(sb, new Identifier(method.getName()), parens, children);

    }

    @Override
    public int getChildCount() {
        return children.length;
    }

    @Override
    public Expression getChild(int i) {
        return children[i];
    }
}
