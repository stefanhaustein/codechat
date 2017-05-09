package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;

import java.util.ArrayList;
import java.util.List;

public class OnInstance extends Instance implements Property.PropertyListener {
    private List<Property> properties = new ArrayList<>();
    private Object lastValue = Boolean.FALSE;
    private OnExpression onExpression;

    public OnInstance(Environment environment, int id) {
        super(environment, id);
    }

    public void init(OnExpression onStatement, EvaluationContext context) {
        detach();
        this.onExpression = onStatement;
        addAll(onStatement.condition, context);
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        Object conditionValue = onExpression.condition.eval(new EvaluationContext(environment, 0));
        if (!conditionValue.equals(lastValue)) {
            lastValue = conditionValue;
            if (Boolean.TRUE.equals(conditionValue)) {
                onExpression.body.eval(new EvaluationContext(environment, 0));
            }
        }
    }

    public void detach() {
        for (Property property : properties) {
            property.removeListener(this);
        }
    }

    private void addAll(Expression expr, EvaluationContext context) {
        if (expr instanceof PropertyAccess) {
            Property property = ((PropertyAccess) expr).getProperty(context);
            property.addListener(this);
            properties.add(property);
        }
        for (int i = 0; i < expr.getChildCount(); i++) {
            addAll(expr.getChild(i), context);
        }
    }

    public void delete() {
        detach();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("on#").append(id);
        sb.append(' ').append(onExpression.condition).append(" {\n");
        onExpression.body.toString(sb, 1);
        sb.append("}\n");
        return sb.toString();
    }

}
