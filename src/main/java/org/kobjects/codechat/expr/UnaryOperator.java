package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class UnaryOperator extends Expression {

    char name;
    Expression operand;

    public UnaryOperator(char name, Expression operand) {
        this.name = name;
        this.operand = operand;
    }

    @Override
    public Object eval(Context context) {
        double value = ((Number) operand.eval(context)).doubleValue();
        switch (name) {
            case '\u221a':
                return Math.sqrt(value);
            case '+':
                return value;
            case '-':
                return -value;
            default:
                throw new RuntimeException("impossible");
        }
    }

    @Override
    public Expression resolve(Scope scope) {
        operand = operand.resolve(scope);
        if (!operand.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Operand must be number.");
        }
        return this;
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_SIGN;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(name);
        operand.toString(sb, getPrecedence());
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
