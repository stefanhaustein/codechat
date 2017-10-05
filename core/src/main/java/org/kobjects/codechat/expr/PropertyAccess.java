package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Tuple;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class PropertyAccess extends Expression {
    Expression base;
    TupleType.PropertyDescriptor property;

    public PropertyAccess(Expression left, TupleType.PropertyDescriptor property) {
        this.base = left;
        this.property = property;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return property.get((Tuple) base.eval(context));
    }

    public Property getProperty(EvaluationContext context) {
        return property.getProperty((Tuple) base.eval(context));
    }

    public boolean isAssignable() {
        return property.writable;
    }

    @Override
    public Property getLock(EvaluationContext context) {
        return property.getProperty((Tuple) base.eval(context));
    }

    @Override
    public void assign(EvaluationContext context, Object value) {
        property.set((Tuple) base.eval(context), value);
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
        sb.append(property.name);
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
