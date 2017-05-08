package org.kobjects.codechat.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.kobjects.codechat.expr.VariableNode;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.ParsingContext;

public class DeleteStatement extends AbstractStatement {
    Expression expr;
    ParsingContext parsingContext;

    public DeleteStatement(Expression expr, ParsingContext parsingContext) {
        this.expr = expr;
        this.parsingContext = parsingContext;
    }

    @Override
    public Object eval(EvaluationContext context) {
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
            parsingContext.variables.remove(varNode.variable.getName());
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

        indent(sb, indent);
        sb.append("delete ");
        expr.toString(sb, indent);
        sb.append(";\n");
    }
}
