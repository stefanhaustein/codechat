package org.kobjects.codechat.statement;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import org.kobjects.codechat.expr.LocalVariableNode;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.RootVariable;

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
        if (o instanceof Instance) {
            ((Instance) o).delete();
        }
        if (expr instanceof RootVariableNode) {
            RootVariableNode varNode = (RootVariableNode) expr;
            varNode.rootVariable.value = null;
        }
/*
        if (expr instanceof Identifier) {
            environment.variables.remove(((Identifier) expr).name);
        }
        */
        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {

        indent(sb, indent);
        sb.append("delete ");
        expr.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        expr.getDependencies(environment, result);
    }
}
