package org.kobjects.codechat.statement;

import com.sun.org.apache.bcel.internal.generic.ISHR;
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
            context.environment.constants.remove(o);
            ((Instance) o).delete();
        }
        if (expr instanceof RootVariableNode) {
            RootVariableNode varNode = (RootVariableNode) expr;
            if (varNode.rootVariable.name.equals(context.environment.constants.get(o))) {
                context.environment.constants.remove(o);
            }
            context.environment.rootVariables.remove(varNode.rootVariable.name);
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
    public void toString(AnnotatedStringBuilder sb, int indent) {

        indent(sb, indent);
        sb.append("delete ");
        expr.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expr.getDependencies(result);
    }
}
