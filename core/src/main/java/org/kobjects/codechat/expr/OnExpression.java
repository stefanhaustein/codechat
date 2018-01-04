package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Closure;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.Statement;

public class OnExpression extends Expression {

    public final OnInstance.OnInstanceType type;
    private final int id;
    public final Expression expression;
    public final Statement body;
    public final Closure closure;

    public OnExpression(OnInstance.OnInstanceType type, int id, Expression condition, Statement body, Closure closure) {
        this.type = type;
        this.id = id;
        this.expression = condition;
        this.body = body;
        this.closure = closure;
    }

    @Override
    public Object eval(EvaluationContext context) {
        OnInstance result = (OnInstance) context.environment.getOrCreateInstance(type, id);
        EvaluationContext template = closure.createEvalContext(context);
        result.init(this, template);
        return result;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append(type.toString().toLowerCase());
        if (id != -1) {
            sb.append('#').append(id);
        }
        sb.append(' ');
        expression.toString(sb, indent);
        sb.append(":\n");
        body.toString(sb, indent + 1);
      sb.indent(indent);
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

    public void getDependencies(DependencyCollector result) {
        super.getDependencies(result);
        body.getDependencies(result);
    }

    @Override
    public Expression reconstruct(Expression... children) {
        return new OnExpression(type, id, children[0], body, closure);
    }
}
