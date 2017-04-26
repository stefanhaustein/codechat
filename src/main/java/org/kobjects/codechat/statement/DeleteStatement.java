package org.kobjects.codechat.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.expr.VariableNode;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Scope;

public class DeleteStatement extends AbstractStatement {
    Expression expr;
    Scope scope;

    public DeleteStatement(Expression expr, Scope scope) {
        this.expr = expr;
        this.scope = scope;
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

        if (expr instanceof VariableNode) {
            VariableNode varNode = (VariableNode) expr;
            context.variables[varNode.variable.getIndex()] = null;
            scope.variables.remove(varNode.variable.getName());
        }

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
