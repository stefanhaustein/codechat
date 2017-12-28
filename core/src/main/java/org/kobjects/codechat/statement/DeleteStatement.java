package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.parser.ParsingContext;

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
            ((RootVariableNode) expr).rootVariable.delete();
        }
/*
        if (expr instanceof Identifier) {
            environment.variables.remove(((Identifier) expr).name);
        }
        */
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {

        sb.indent(indent);
        sb.append("delete ");
        expr.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expr.getDependencies(result);
    }
}
