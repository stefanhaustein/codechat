package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.OnchangeExpression;

public class OnchangeInstance extends Instance implements Property.PropertyListener {
    private OnchangeExpression onchangeExpression;
    private Property property;

    public OnchangeInstance(Environment environment, int id) {
        super(environment, id);
    }

    public void init(OnchangeExpression onchangeExpression, Context context) {
        detach();
        this.onchangeExpression = onchangeExpression;
        property = onchangeExpression.propertyExpr.getProperty(context);
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
        onchangeExpression.body.eval(environment.getRootContext());
    }

}
