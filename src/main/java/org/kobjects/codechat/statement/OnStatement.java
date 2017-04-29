package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.api.Ticking;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Property;

import java.util.ArrayList;
import java.util.List;

public class OnStatement extends StatementInstance implements Property.PropertyListener {
    private Expression condition;
    private Statement body;
    private List<Property> properties = new ArrayList<>();
    private Object lastValue = Boolean.FALSE;

    public OnStatement(Environment environment, int id) {
        super(environment, id);
    }

    public void init(Expression condition, Statement body) {
        this.condition = condition;
        this.body = body;
    }


    public void detach() {
        for (Property property : properties) {
            property.removeListener(this);
        }
    }
    public void delete() {
        detach();
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        Object conditionValue = condition.eval(environment.getRootContext());
        if (!conditionValue.equals(lastValue)) {
            lastValue = conditionValue;
            if (Boolean.TRUE.equals(conditionValue)) {
                body.eval(environment.getRootContext());
            }
        }
    }

    @Override
    public Object eval(Context context) {
        detach();
        addAll(condition, context);
        return null;
    }


    private void addAll(Expression expr, Context context) {
        if (expr instanceof PropertyAccess) {
            Property property = ((PropertyAccess) expr).getProperty(context);
            property.addListener(this);
            properties.add(property);
        }
        for (int i = 0; i < expr.getChildCount(); i++) {
            addAll(expr.getChild(i), context);
        }
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
