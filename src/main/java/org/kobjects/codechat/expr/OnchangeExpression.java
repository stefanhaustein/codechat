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
    private int[] closureMap;

    public OnchangeExpression(int id, PropertyAccess property, Statement body, int varCount, int[] closureMap) {
        this.id = id;
        this.varCount = varCount;
        this.propertyExpr = property;
        this.body = body;
        this.closureMap = closureMap;
    }

    @Override
    public Object eval(EvaluationContext context) {
        OnchangeInstance result = (OnchangeInstance) context.environment.getInstance(
                Type.forJavaClass(OnchangeInstance.class), id, true);
        Object[] template = new Object[varCount];
        for (int i = 0; i < closureMap.length; i+=2) {
            template[closureMap[i + 1]] = context.variables[closureMap[i]];
        }
        result.init(this, template);
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
