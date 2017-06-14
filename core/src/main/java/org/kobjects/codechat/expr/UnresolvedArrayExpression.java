package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

import static org.kobjects.codechat.lang.Parser.PRECEDENCE_PATH;

public class UnresolvedArrayExpression extends AbstractUnresolved {

    Expression base;
    Expression[] arguments;

    public UnresolvedArrayExpression(Expression base, Expression[] arguments) {
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
            throw new RuntimeException("Array index must be number");
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

    @Override
    public int getChildCount() {
        return arguments.length + 1;
    }

    @Override
    public Expression getChild(int index) {
        return  index == 0 ? base : arguments[index - 1];
    }
}
