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
    private EvaluationContext contextTemplate;

    public OnInstance(Environment environment, int id) {
        super(environment, id);
    }

    public void init(OnExpression onExpression, EvaluationContext contextTemplate) {
        detach();
        this.onExpression = onExpression;
        this.contextTemplate = contextTemplate;
        addAll(onExpression.expression, contextTemplate);
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        if (onExpression.onChange) {
            EvaluationContext evalContext = contextTemplate.clone();
            onExpression.body.eval(evalContext);
        } else {
            Object conditionValue = onExpression.expression.eval(contextTemplate);
            if (!conditionValue.equals(lastValue)) {
                lastValue = conditionValue;
                if (Boolean.TRUE.equals(conditionValue)) {
                    EvaluationContext evalContext = contextTemplate.clone();
                    onExpression.body.eval(evalContext);
                }
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

        boolean wrap = onExpression.closure.getMappingCount() != 0;

        if (wrap) {
            sb.append("{\n");
            for (Closure.Mapping mapping : onExpression.closure.getMappings()) {
                sb.append("  var ").append(mapping.name).append(" = ");
                sb.append(contextTemplate.variables[mapping.closureIndex]).append("\n");
            }
            sb.append("  ");
        }
        sb.append(onExpression.onChange ? "onchange#" : "on#").append(id);
        sb.append(' ').append(onExpression.expression).append(" {\n");
        onExpression.body.toString(sb, wrap ? 2 : 1);
        if (wrap) {
            sb.append("  }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

}
