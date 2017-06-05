package org.kobjects.codechat.expr;


import java.util.List;
import org.kobjects.codechat.lang.CollectionType;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class ArrayIndex extends Expression {

    Expression base;
    Expression index;

    public ArrayIndex(Expression base, Expression index) {
        this.base = base;
        this.index = index;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return ((List) base.eval(context)).get(((Number) index.eval(context)).intValue());
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        base = base.resolve(parsingContext);
        if (!(base.getType() instanceof CollectionType)) {
            throw new RuntimeException("Array expected for index access");
        }
        index = index.resolve(parsingContext);
        if (!index.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Array index must be a number.");
        }
        return this;
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
    public void toString(StringBuilder sb, int indent) {
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
