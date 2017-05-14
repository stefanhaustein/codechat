package org.kobjects.codechat.expr;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.MutableProperty;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.ParsingContext;
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
    public Object eval(EvaluationContext context) {
        return getProperty(context).get();
    }

    public boolean isAssignable() {
        return MutableProperty.class.isAssignableFrom(property.getType());
    }


    public Property getProperty(EvaluationContext context) {
        Object baseValue = base.eval(context);
        try {
            return (Property) property.get(baseValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void assign(EvaluationContext context, Object value) {
        MutableProperty p = (MutableProperty) getProperty(context);
        p.set(value);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        base = base.resolve(parsingContext);
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
        java.lang.reflect.Type propertyType = ((ParameterizedType) property.getGenericType()).getActualTypeArguments()[0];
        return Type.forJavaType(propertyType);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(StringBuilder sb, int indent) {
        base.toString(sb, 0, Parser.PRECEDENCE_PATH);
        sb.append('.');
        sb.append(name);
    }

    @Override
    public int getChildCount() {
        return 1;
    }


    @Override
    public Expression getChild(int index) {
        return base;
    }


}
