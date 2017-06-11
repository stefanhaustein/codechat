package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.JavaType;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class PropertyAccess extends Expression {
    String name;
    Expression base;
    JavaType.Property property;

    public PropertyAccess(Expression left, Expression right) {
        if (!(right instanceof Identifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((Identifier) right).name;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return property.get(base.eval(context));
    }

    public Property getProperty(EvaluationContext context) {
        return property.getProperty(base.eval(context));
    }

    public boolean isAssignable() {
        return property.isMutable();
    }


    @Override
    public void assign(EvaluationContext context, Object value) {
        property.set(base.eval(context), value);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        base = base.resolve(parsingContext);
        JavaType instanceType = (JavaType) base.getType();
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
