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
    public void toString(StringBuilder sb, int parentPrecedence) {
        boolean braces = parentPrecedence > Parser.PRECEDENCE_IMPLICIT;
        if (braces) {
            sb.append('(');
        }
        sb.append("create ").append(type.toString());
        if (braces) {
            sb.append(')');
        }
    }
}
