package org.kobjects.codechat.expr;


import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.parser.Parser.PRECEDENCE_PATH;

public class ArrayIndex extends Expression {

    Expression base;
    Expression index;

    public ArrayIndex(Expression base, Expression index) {
        this.base = base;
        this.index = index;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return ((Collection) base.eval(context)).get(((Number) index.eval(context)).intValue());
    }

    @Override
    public Type getType() {
        return ((CollectionType) base.getType()).elementType;
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        base.toString(sb, indent);
        sb.append('[');
        index.toString(sb, indent);
        sb.append(']');
    }

    @Override
    public int getChildCount() {
        return 2;
    }
}
