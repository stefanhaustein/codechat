package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;

import java.util.ArrayList;
import java.util.List;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.Instantiable;

public class OnInstance implements Instance, Property.PropertyListener {
    public static final OnInstanceType ON_TYPE = new OnInstanceType("on");
    public static final OnInstanceType ONCHANGE_TYPE = new OnInstanceType("onchange");

    private List<Property> properties = new ArrayList<>();
    private Object lastValue = Boolean.FALSE;
    private OnExpression onExpression;
    private EvaluationContext contextTemplate;
    private int id;

    public OnInstance(Environment environment, int id) {
        this.id = id;
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

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void serializeDeclaration(StringBuilder sb, List<Annotation> annotations) {

    }

    @Override
    public void serializeDefinition(StringBuilder sb, boolean all) {
        boolean wrap = onExpression.closure.toString(sb, contextTemplate);

        sb.append(onExpression.onChange ? "onchange#" : "on#").append(getId());
        sb.append(' ').append(onExpression.expression).append(" {\n");
        onExpression.body.toString(sb, wrap ? 2 : 1);
        if (wrap) {
            sb.append("  }\n");
        }
        sb.append("}\n");
    }

    public void delete() {
        detach();
    }


    @Override
    public OnInstanceType getType() {
        return onExpression.onChange ? ONCHANGE_TYPE : ON_TYPE;
    }

    public static class OnInstanceType extends Type implements Instantiable<OnInstance> {
        private final String name;

        OnInstanceType(String name) {
            this.name = name;
        }

        @Override
        public OnInstance createInstance(Environment environment, int id) {
            return new OnInstance(environment, id);
        }

        @Override
        public boolean isAssignableFrom(Type other) {
            return false;
        }

        @Override
        public String getName() {
            return name;
        }
    }

}
