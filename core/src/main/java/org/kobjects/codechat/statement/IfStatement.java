package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Environment;

public class IfStatement extends AbstractStatement {
    Environment environment;
    public Expression condition;
    public Statement body;

    public IfStatement(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        if (Boolean.TRUE.equals(condition.eval(context))) {
            body.eval(context);
        }
        return KEEP_GOING;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("if ").append(condition).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }
}
