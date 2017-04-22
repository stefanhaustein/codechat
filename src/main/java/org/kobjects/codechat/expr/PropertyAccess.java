package org.kobjects.codechat.expr;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class PropertyAccess extends Expression {
    String name;
    Expression base;
    Field property;

    public PropertyAccess(Expression left, Expression right) {
        if (!(right instanceof Identifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((Identifier) right).name;
    }

    @Override
    public Object eval(Context context) {
        Object value = base.eval(context);
        try {
            Property p = (Property) property.get(value);
            return p.get();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(Context context, Object value) {
        Object baseValue = base.eval(context);
        try {
            Property p = (Property) property.get(baseValue);
            p.set(value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Expression resolve(Scope scope) {
        base = base.resolve(scope);
        Class c = base.getType().getJavaClass();
        try {
            property = c.getField(name);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        if (!Property.class.isAssignableFrom(property.getType())) {
            throw new RuntimeException("Property " + name + " not found (not of type Property but: " + property.getType());
        }
        return this;
    }

    @Override
    public Type getType() {
        Class propertyType = (Class) ((ParameterizedType) property.getGenericType()).getActualTypeArguments()[0];
        return Type.forJavaClass(propertyType);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(StringBuilder sb) {
        base.toString(sb, Parser.PRECEDENCE_PATH);
        sb.append('.');
        sb.append(name);
    }
}
