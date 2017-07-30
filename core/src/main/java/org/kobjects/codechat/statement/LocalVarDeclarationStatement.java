package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.Environment;
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
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("var ").append(variable.getName()).append(" = ");
        initializer.toString(sb, indent);
        sb.append(";\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        initializer.getDependencies(environment, result);
    }
}
