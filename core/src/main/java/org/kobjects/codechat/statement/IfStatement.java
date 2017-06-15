package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
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
        AbstractStatement.indent(sb, indent);
        sb.append("if ").append(condition).append(" {\n");
        ifBody.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        if (elseBody != null) {
            sb.append("} else {\n");
            elseBody.toString(sb, indent + 1);
            AbstractStatement.indent(sb, indent);
        }
        sb.append("}\n");
    }
}
