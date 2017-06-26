package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Tuple;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class UnresolvedPropertyAccess extends UnresolvedExpression {
    String name;
    UnresolvedExpression base;

    public UnresolvedPropertyAccess(UnresolvedExpression left, UnresolvedExpression right) {
        if (!(right instanceof UnresolvedIdentifier)) {
            throw new IllegalArgumentException("Right node must be identifier");
        }
        this.base = left;
        this.name = ((UnresolvedIdentifier) right).name;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Expression base = this.base.resolve(parsingContext);
        TupleType instanceType = (TupleType) base.getType();
        TupleType.PropertyDescriptor property = instanceType.getProperty(name);
        return new PropertyAccess(base, property);
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
}
