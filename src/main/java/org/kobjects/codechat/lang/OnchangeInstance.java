package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.OnchangeExpression;

public class OnchangeInstance extends Instance implements Property.PropertyListener {
    private OnchangeExpression onchangeExpression;
    private Property property;
    private Object[] contextTemplate;


    public OnchangeInstance(Environment environment, int id) {
        super(environment, id);
    }

    public void init(OnchangeExpression onchangeExpression, Object[] contextTemplate) {
        detach();
        this.onchangeExpression = onchangeExpression;
        this.contextTemplate = contextTemplate;
        property = onchangeExpression.propertyExpr.getProperty(new EvaluationContext(environment, contextTemplate));
        property.addListener(this);
    }


    public void detach() {
        if (property != null) {
            property.removeListener(this);
        }
    }

    public void delete() {
        detach();
    }

    @Override
    public void valueChanged(Property property, Object oldValue, Object newValue) {
        onchangeExpression.body.eval(new EvaluationContext(environment, contextTemplate));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("onchange#").append(id);
        sb.append(' ').append(onchangeExpression.propertyExpr).append(" {\n");
        onchangeExpression.body.toString(sb, 1);
        sb.append("}\n");
        return sb.toString();
    }

}
