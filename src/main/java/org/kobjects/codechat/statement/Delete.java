package org.kobjects.codechat.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.Node;

public class Delete extends AbstractStatement {
    Node expr;

    public Delete(Node expr) {
        this.expr = expr;
    }

    @Override
    public Object eval(Environment environment) {
        Object o = expr.eval(environment);
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

        if (expr instanceof Identifier) {
            environment.variables.remove(((Identifier) expr).name);
        }
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append("delete ").append(expr).append(";");
    }
}
