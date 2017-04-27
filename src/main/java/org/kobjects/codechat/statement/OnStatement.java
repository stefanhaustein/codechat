package org.kobjects.codechat.statement;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.api.Ticking;
import org.kobjects.codechat.expr.Expression;

public class OnStatement extends StatementInstance implements Ticking {
    public Expression condition;
    public Statement body;

    public OnStatement(Environment environment, int id) {
        super(environment, id);
    }

    @Override
    public void tick(boolean force) {
        if (Boolean.TRUE.equals(condition.eval(environment.getRootContext()))) {
            body.eval(environment.getRootContext());
        }
    }


    @Override
    public Object eval(Context context) {
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
