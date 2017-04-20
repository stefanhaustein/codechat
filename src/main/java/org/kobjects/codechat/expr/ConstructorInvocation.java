package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Type;

public class ConstructorInvocation extends Resolved {

    Type type;

    ConstructorInvocation(Type type) {
        this.type = type;
    }

    @Override
    public Object eval(Context context) {
        return context.environment.instantiate(type.getJavaClass());
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_IMPLICIT;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append("create ").append(type.toString());
    }
}
