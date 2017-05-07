package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class OnExpression extends AbstractResolved {
    public final Expression condition;
    public final Statement body;

    private final int id;

    public OnExpression(int id, Expression condition, Statement body) {
        this.id = id;
        this.condition = condition;
        this.body = body;
    }

    @Override
    public Object eval(Context context) {
        OnInstance result = (OnInstance) context.environment.getInstance(
                Type.forJavaClass(OnInstance.class), id, true);
        result.init(this, context);
        return result;
    }

    @Override
    public Type getType() {
        return Type.forJavaClass(OnInstance.class);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb) {
        int indent = 1; // FIXME!
        AbstractStatement.indent(sb, indent);
        sb.append("on");
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append(' ').append(condition).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }

    @Override
    public int getChildCount() {
        return 1;
    }
}
