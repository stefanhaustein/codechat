package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Variable;

public class VarStatement extends AbstractStatement {
    Variable variable;
    Expression initializer;

    public VarStatement(Variable variable, Expression initializer) {
        this.variable = variable;
        this.initializer = initializer;
    }

    @Override
    public Object eval(EvaluationContext context) {
        context.variables[variable.getIndex()] = initializer.eval(context);
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("var ").append(variable.getName()).append(" = ");
        initializer.toString(sb, indent);
        sb.append(";\n");
    }
}
