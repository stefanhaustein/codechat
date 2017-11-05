package org.kobjects.codechat.expr;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.HasDependencies;
import org.kobjects.codechat.type.Type;

public abstract class Expression implements HasDependencies {

    public abstract Object eval(EvaluationContext context);

    public void assign(EvaluationContext context, Object value) {
        throw new RuntimeException("Assignment not supported for " + this);
    }

    public Object getLock(EvaluationContext context) {
        throw new RuntimeException("synchronization not supported for " + this);
    }

    public final String toString() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
        toString(asb, 0, 0);
        return asb.toString();
    }

    public abstract Type getType();

    public abstract int getPrecedence();

    public abstract void toString(AnnotatedStringBuilder asb, int indent);

    public final void toString(AnnotatedStringBuilder asb, int indent, int parentPrecedence) {
        if (parentPrecedence > getPrecedence()) {
            asb.append('(');
            toString(asb, indent);
            asb.append(')');
        } else {
            toString(asb, indent);
        }
    }

    public abstract int getChildCount();

    public Expression getChild(int i) {
        throw new UnsupportedOperationException();
    }

    public boolean isAssignable() {
        return false;
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        for (int i = 0; i < getChildCount(); i++) {
            getChild(i).getDependencies(result);
        }
    }

    public abstract Expression reconstruct(Expression... children);
}
