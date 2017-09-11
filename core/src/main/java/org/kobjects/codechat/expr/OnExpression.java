package org.kobjects.codechat.expr;

import java.util.Collection;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class OnExpression extends Expression {
    public final boolean onChange;
    private final int id;
    public final Expression expression;
    public final Statement body;
    public final Closure closure;

    public OnExpression(boolean onChange, int id, Expression condition, Statement body, Closure closure) {
        this.onChange = onChange;
        this.id = id;
        this.expression = condition;
        this.body = body;
        this.closure = closure;
    }

    @Override
    public Object eval(EvaluationContext context) {
        OnInstance result = (OnInstance) context.environment.getInstance(
                onChange ? OnInstance.ONCHANGE_TYPE : OnInstance.ON_TYPE, id, true);
        EvaluationContext template = closure.createEvalContext(context);
        result.init(this, template);
        return result;
    }

    @Override
    public Type getType() {
        return onChange ? OnInstance.ONCHANGE_TYPE : OnInstance.ON_TYPE;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(onChange ? "onchange" : "on");
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append(' ').append(expression).append(":\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("end");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        return expression;
    }

    public void getDependencies(Environment environment, Collection<Entity> result) {
        super.getDependencies(environment, result);
        body.getDependencies(environment, result);
    }
}
