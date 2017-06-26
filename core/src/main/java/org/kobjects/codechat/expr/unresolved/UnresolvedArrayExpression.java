package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.ArrayIndex;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class UnresolvedArrayExpression extends UnresolvedExpression {

    UnresolvedExpression base;
    UnresolvedExpression[] arguments;

    public UnresolvedArrayExpression(UnresolvedExpression base, UnresolvedExpression[] arguments) {
        this.base = base;
        this.arguments = arguments;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Expression[] resolvedArguments = new Expression[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            resolvedArguments[i] = arguments[i].resolve(parsingContext);
        }

        if (resolvedArguments.length != 1) {
            throw new RuntimeException("Exactly one array index expected");
        }
        if (resolvedArguments[0].getType() != Type.NUMBER) {
            throw new RuntimeException("List index must be number");
        }
        return new ArrayIndex(base.resolve(parsingContext), resolvedArguments[0]);
    }

    @Override
    public int getPrecedence() {
        return PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        base.toString(sb, indent);
        sb.append('[');
        if (arguments.length > 0) {
            arguments[0].toString(sb, indent);
            for (int i = 1; i < arguments.length; i++) {
                sb.append(", ");
                arguments[i].toString(sb, indent);
            }
        }
        sb.append(']');
    }
}
