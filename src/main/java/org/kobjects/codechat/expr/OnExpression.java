package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class OnExpression extends AbstractResolved {
    public final Expression condition;
    public final Statement body;

    private final int id;
    private int varCount;
    private int[] closureMap;

    public OnExpression(int id, Expression condition, Statement body, int varCount, int[] closureMap) {
        this.id = id;
        this.condition = condition;
        this.body = body;
        this.varCount = varCount;
        this.closureMap = closureMap;
    }

    @Override
    public Object eval(EvaluationContext context) {
        OnInstance result = (OnInstance) context.environment.getInstance(
                Type.forJavaClass(OnInstance.class), id, true);
        Object[] template = new Object[varCount];
        for (int i = 0; i < closureMap.length; i+=2) {
            template[closureMap[i + 1]] = context.variables[closureMap[i]];
        }
        result.init(this, template);
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
    public void toString(StringBuilder sb, int indent) {
        sb.append("on");
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append(' ').append(condition).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}");
    }

    @Override
    public int getChildCount() {
        return 1;
    }
}
