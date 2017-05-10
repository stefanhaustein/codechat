package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.OnchangeInstance;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class OnchangeExpression extends AbstractResolved {
    private final int id;
    public PropertyAccess propertyExpr;
    public final Statement body;
    private final int varCount;

    public OnchangeExpression(int id, PropertyAccess property, Statement body, int varCount) {
        this.id = id;
        this.varCount = varCount;
        this.propertyExpr = property;
        this.body = body;
    }

    @Override
    public Object eval(EvaluationContext context) {
        OnchangeInstance result = (OnchangeInstance) context.environment.getInstance(
                Type.forJavaClass(OnchangeInstance.class), id, true);
        result.init(this, context, varCount);
        return result;

    }

    @Override
    public Type getType() {
        return Type.forJavaClass(OnchangeInstance.class);
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append("onchange#").append(id).append(' ').append(propertyExpr).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public Expression getChild(int index) {
        return propertyExpr;
    }
}
