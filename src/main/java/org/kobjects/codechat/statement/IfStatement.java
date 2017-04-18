package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.Node;
import org.kobjects.codechat.lang.Environment;

public class IfStatement extends AbstractStatement {
    Environment environment;
    public Node condition;
    public Block body;

    public IfStatement(Node condition, Block body) {
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object eval(Environment environment) {
        if (Boolean.TRUE.equals(condition.eval(environment))) {
            body.eval(environment);
        }
        return null;
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
