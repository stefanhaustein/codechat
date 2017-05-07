package org.kobjects.codechat.expr;

import org.kobjects.codechat.expr.AbstractResolved;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.OnchangeInstance;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;

public class OnchangeExpression extends AbstractResolved {
    private final int id;
    public PropertyAccess propertyExpr;
    public Statement body;

    public OnchangeExpression(int id, PropertyAccess property, Statement body) {
        this.id = id;
        this.propertyExpr = property;
        this.body = body;
    }

    @Override
    public Object eval(Context context) {
        OnchangeInstance result = (OnchangeInstance) context.environment.getInstance(
                Type.forJavaClass(OnchangeInstance.class), id, true);
        result.init(this, context);
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
    public void toString(StringBuilder sb) {
        int indent = 1;  //FIXME
        AbstractStatement.indent(sb, indent);
        sb.append("onchange#").append(id).append(' ').append(propertyExpr).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    public Expression getChild(int index) {
        return propertyExpr;
    }
}
