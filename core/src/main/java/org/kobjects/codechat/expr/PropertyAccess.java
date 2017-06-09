package org.kobjects.codechat.expr;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.InstanceType;
import org.kobjects.codechat.lang.MutableProperty;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

public class PropertyAccess extends Expression {
    String name;
    Expression base;
    InstanceType.Property property;

    public PropertyAccess(Expression left, Expression right) {
        if (!(right instanceof Identifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((Identifier) right).name;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return property.get((Instance) base.eval(context));
    }

    public Property getProperty(EvaluationContext context) {
        return property.getProperty((Instance) base.eval(context));
    }

    public boolean isAssignable() {
        return property.isMutable();
    }


    @Override
    public void assign(EvaluationContext context, Object value) {
        property.set((Instance) base.eval(context), value);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        base = base.resolve(parsingContext);
        InstanceType instanceType = (InstanceType) base.getType();
        property = instanceType.getProperty(name);
        return this;
    }

    @Override
    public Type getType() {
        return property.type;
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
