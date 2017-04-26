package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Type;

public class ConstructorInvocation extends AbstractResolved {

    Type type;
    int id;

    ConstructorInvocation(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public Object eval(Context context) {
        return context.environment.instantiate(type.getJavaClass(), id);
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
        sb.append("new ").append(type.toString());
    }
}
