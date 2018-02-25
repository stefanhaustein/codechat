package org.kobjects.codechat.statement;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.ClassDeclaration;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.FunctionDeclaration;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.instance.Instance;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.Parser;

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
        variable.error = null;
        variable.unparsed = null;

        if (variable.constant && variable.value instanceof Instance) {
            context.environment.constants.put((Instance) variable.value, variable.name);
        }

        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        if (variable.constant && !explicitType) {
            if (expression instanceof FunctionDeclaration) {
                ((FunctionDeclaration) expression).toString(sb, indent, variable.name);
                return;
            }
            if (expression instanceof ClassDeclaration) {
                sb.append("class ").append(variable.name).append(":\n");
                ((ClassDeclaration) expression).printBody(sb, indent);
                sb.indent(indent);
                sb.append("end\n");
                return;
            }
        }
        sb.indent(indent);
        sb.append(variable.constant ? "let " : "variable ");
        sb.append(variable.name);
        if (explicitType) {
            sb.append(": ");
            sb.append(variable.type.toString());
            sb.append(" = ");
        } else {
            sb.append(" := ");
        }
        expression.toString(sb, indent, Parser.PRECEDENCE_EQUALITY);
        sb.append("\n");

    }

    @Override
    public void getDependencies(DependencyCollector result) {
        expression.getDependencies(result);
    }
}
