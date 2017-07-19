package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.UnaryOperator;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedUnaryOperator extends UnresolvedExpression {
    char name;
    UnresolvedExpression operand;

    public UnresolvedUnaryOperator(int start, int end, char name, UnresolvedExpression operand) {
        super(start, end);
        this.name = name;
        this.operand = operand;
    }

    private Type getType() {
        return name == '\u00ac' ? Type.BOOLEAN : Type.NUMBER;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        Expression operand = this.operand.resolve(parsingContext, null);
        if (!operand.getType().equals(getType())) {
            throw new ExpressionParser.ParsingException(start, end, "Operand must be " + getType(), null);
        }
        return new UnaryOperator(name, operand);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_SIGN;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        if (name != '°') {
            sb.append(name);
        }
        operand.toString(sb, 0, getPrecedence());
        if (name == '°') {
            sb.append(name);
        }
    }

}
