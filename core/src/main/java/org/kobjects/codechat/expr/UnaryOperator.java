package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Type;

public class UnaryOperator extends Expression {
    char name;
    Expression operand;

    public UnaryOperator(char name, Expression operand) {
        this.name = name;
        this.operand = operand;
    }

    @Override
    public Object eval(EvaluationContext context) {
        if (name == '\u00ac') {
            return Boolean.FALSE.equals(operand.eval(context));
        }
        double value = ((Number) operand.eval(context)).doubleValue();
        switch (name) {
            case '\u221a':
                return Math.sqrt(value);
            case '+':
                return value;
            case '-':
                return -value;
            case '°':
                return value * Math.PI / 180;
            default:
                throw new RuntimeException("impossible");
        }
    }

    @Override
    public Type getType() {
        return name == '\u00ac' ? Type.BOOLEAN : Type.NUMBER;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_SIGN;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        if (name == '°') {
            operand.toString(sb, indent, getPrecedence());
            sb.append(name);
        } else {
            if (name == '\u00ac') {
                sb.append("not ");
            } else {
                sb.append(name);
            }
            operand.toString(sb, indent, getPrecedence());
        }
    }

    @Override
    public int getChildCount() {
        return 1;
    }

    @Override
    public Expression getChild(int index) {
        return operand;
    }
}
