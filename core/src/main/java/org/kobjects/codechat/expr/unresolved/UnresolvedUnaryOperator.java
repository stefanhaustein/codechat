package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.UnaryOperator;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class UnresolvedUnaryOperator extends UnresolvedExpression {
    char name;
    UnresolvedExpression operand;

    public UnresolvedUnaryOperator(char name, UnresolvedExpression operand) {
        this.name = name;
        this.operand = operand;
    }

    private Type getType() {
        return name == '\u00ac' ? Type.BOOLEAN : Type.NUMBER;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Expression operand = this.operand.resolve(parsingContext);
        if (!operand.getType().equals(getType())) {
            throw new RuntimeException("Operand must be " + getType());
        }
        return new UnaryOperator(name, operand);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_SIGN;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(name);
        operand.toString(sb, 0, getPrecedence());
    }

}
