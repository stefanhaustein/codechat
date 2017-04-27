package org.kobjects.codechat.statement;

import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Property;

public class OnchangeStatement extends StatementInstance implements Property.PropertyListener {
    private PropertyAccess propertyExpr;
    private Statement body;
    private Property property;

    protected OnchangeStatement(Environment environment, int id) {
        super(environment, id);
    }

    public void init(PropertyAccess property, Statement body) {
        detach();
        this.propertyExpr = property;
        this.body = body;
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
        body.eval(environment.getRootContext());
    }

    @Override
    public Object eval(Context context) {
        detach();
        property = propertyExpr.getProperty(context);
        property.addListener(this);
        return null;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        AbstractStatement.indent(sb, indent);
        sb.append("onchange#").append(id).append(' ').append(propertyExpr).append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");
    }

}
