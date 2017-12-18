package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.FunctionType;

public class RootVarDeclarationStatement extends AbstractStatement {
    private final RootVariable variable;
    private final Expression expression;
    private final boolean explicitType;

    public RootVarDeclarationStatement(RootVariable variable, Expression expression, boolean explicitType) {
        this.variable = variable;
        this.expression = expression;
        this.explicitType = explicitType;
    }

    @Override
    public Object eval(EvaluationContext context) {
        variable.value = expression.eval(context);
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        if (variable.constant && variable.type instanceof FunctionType && expression instanceof FunctionExpression) {
            ((FunctionExpression) expression).toString(sb, indent + 4, variable.name);
            return;
        }
        indent(sb, indent);
        sb.append(variable.constant ? "let " : "variable ");
        sb.append(variable.name);
        if (explicitType) {
            sb.append(": ");
            sb.append(variable.type.getName());
        }
        sb.append(" = ");
        expression.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append("\n");

    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expression.getDependencies(result);
    }
}
