package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.instance.Instance;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.instance.Property;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.Type;

public class PropertyAccessExpr extends Expression {
    Expression base;
    public Classifier.PropertyDescriptor property;

    public PropertyAccessExpr(Expression left, Classifier.PropertyDescriptor property) {
        this.base = left;
        this.property = property;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return property.get((Instance) base.eval(context));
    }

    public Property getProperty(EvaluationContext context) {
        try {
            return property.getProperty((Instance) base.eval(context));
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("property: " + property);
            System.err.println("base: " +base);
            System.err.println("context: " + context);
            throw new RuntimeException(e);
        }
    }

    public boolean isAssignable() {
        return property.writable;
    }

    @Override
    public Property getLock(EvaluationContext context) {
        return property.getProperty((Instance) base.eval(context));
    }

    @Override
    public void assign(EvaluationContext context, Object value) {
        property.set((Instance) base.eval(context), value);
    }

    @Override
    public Type getType() {
        return property.type;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    public void toString(AnnotatedStringBuilder sb, int indent) {
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

    @Override
    public PropertyAccessExpr reconstruct(Expression... children) {
        return new PropertyAccessExpr(children[0], property);
    }
}
