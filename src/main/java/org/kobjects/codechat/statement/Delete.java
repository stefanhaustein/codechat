package org.kobjects.codechat.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.Expression;

public class Delete extends AbstractStatement {
    Expression expr;

    public Delete(Expression expr) {
        this.expr = expr;
    }

    @Override
    public Object eval(Context context) {
        Object o = expr.eval(context);
        try {
            Method delete = o.getClass().getMethod("delete");
            try {
                delete.invoke(o);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e.getCause());
            }
        } catch (NoSuchMethodException e) {
            // ok
        }

        System.err.println("Variable deletion missing here!");

/*
        if (expr instanceof Identifier) {
            environment.variables.remove(((Identifier) expr).name);
        }
        */
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append("delete ").append(expr).append(";");
    }
}
