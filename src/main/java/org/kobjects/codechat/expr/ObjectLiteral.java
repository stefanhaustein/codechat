package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

public class ObjectLiteral extends Expression {


    @Override
    public Object eval(EvaluationContext context) {
        throw new RuntimeException("NYI");
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        throw new RuntimeException("NYI");
    }

    @Override
    public Type getType() {
        throw new RuntimeException("NYI");
    }

    @Override
    public int getPrecedence() {
        throw new RuntimeException("NYI");
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        throw new RuntimeException("NYI");

    }

    @Override
    public int getChildCount() {
        throw new RuntimeException("NYI");
    }
}
