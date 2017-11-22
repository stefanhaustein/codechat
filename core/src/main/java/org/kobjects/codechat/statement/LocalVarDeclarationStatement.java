package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.LocalVariable;

public class LocalVarDeclarationStatement extends AbstractStatement {
    LocalVariable variable;
    Expression initializer;

    public LocalVarDeclarationStatement(LocalVariable variable, Expression initializer) {
        this.variable = variable;
        this.initializer = initializer;
    }

    @Override
    public Object eval(EvaluationContext context) {
        context.variables[variable.getIndex()] = initializer.eval(context);
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append(variable.constant ? "let ": "variable ").append(variable.getName()).append(" = ");
        initializer.toString(sb, indent + 4);
        sb.append("\n");
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        initializer.getDependencies(result);
    }
}
