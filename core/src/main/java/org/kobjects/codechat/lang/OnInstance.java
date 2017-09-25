package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.PropertyAccess;

import java.util.ArrayList;
import java.util.List;

import org.kobjects.codechat.type.Type;

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


    public void delete() {
        detach();
    }


    @Override
    public OnInstanceType getType() {
        return onExpression.onChange ? ONCHANGE_TYPE : ON_TYPE;
    }

    @Override
    public void serializeStub(AnnotatedStringBuilder asb) {
        asb.append("new ");
        Formatting.toLiteral(asb, this);
        asb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.serializeDependencies(asb, this);

        boolean wrap = onExpression.closure.toString(asb.getStringBuilder(), contextTemplate);

        asb.append((onExpression.onChange ? "onchange#" : "on#") + String.valueOf(getId()), new EntityLink(this));
        asb.append(" ").append(onExpression.expression.toString()).append(":\n");
        onExpression.body.toString(asb.getStringBuilder(), wrap ? 2 : 1);
        if (wrap) {
            asb.append("  end;\n");
        }
        asb.append("end;\n");
    }

    public static class OnInstanceType extends Type {
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

    @Override
    public void getDependencies(DependencyCollector result) {
        onExpression.getDependencies(result);
    }
}
