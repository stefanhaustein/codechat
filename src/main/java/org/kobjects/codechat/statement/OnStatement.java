package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.api.Ticking;
import org.kobjects.codechat.expr.Node;

public class OnStatement extends Instance implements Statement, Ticking {
    Environment environment;
    public Node condition;
    public Block body;

    public OnStatement(Environment environment, int id, Node condition, Block body) {
        super(environment, id);
        this.environment = environment;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public void tick(boolean force) {
        if (Boolean.TRUE.equals(condition.eval(environment))) {
            body.eval(environment);
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    @Override
    public Object eval(Environment environment) {
        environment.ticking.add(this);
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("on#").append(id).append(' ').append(condition).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }
}
