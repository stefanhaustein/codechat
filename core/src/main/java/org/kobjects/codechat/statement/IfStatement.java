package org.kobjects.codechat.statement;

import java.util.Collection;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Dependency;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Environment;

public class IfStatement extends AbstractStatement {
    Environment environment;
    public Expression condition;
    public Statement ifBody;
    public Statement elseBody;

    public IfStatement(Expression condition, Statement ifBody, Statement elseBody) {
        this.condition = condition;
        this.ifBody = ifBody;
        this.elseBody = elseBody;
    }

    @Override
    public Object eval(EvaluationContext context) {
        if (Boolean.TRUE.equals(condition.eval(context))) {
            ifBody.eval(context);
        } else if (elseBody != null) {
            elseBody.eval(context);
        }
        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        toString(sb, indent, false);
    }

    public void toString(StringBuilder sb, int indent, boolean elseif) {
        if (elseif) {
            sb.append("elseif ");
        } else {
            AbstractStatement.indent(sb, indent);
            sb.append("if ");
        }
        sb.append(condition).append(":\n");
        ifBody.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        if (elseBody instanceof IfStatement) {
            ((IfStatement) elseBody).toString(sb, indent, true);
        } else if (elseBody != null) {
            sb.append("else:\n");
            elseBody.toString(sb, indent + 1);
            AbstractStatement.indent(sb, indent);
        }
        sb.append("end;\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        condition.getDependencies(environment, result);
        ifBody.getDependencies(environment, result);
        if (elseBody != null) {
            elseBody.getDependencies(environment, result);
        }
    }
}
