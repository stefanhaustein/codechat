package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class BinaryOperator extends Expression {
    public char name;
    public Expression left;
    public Expression right;

    public BinaryOperator(char name, Expression left, Expression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(Context context) {
        if (name == '$') {
            return (String) left.eval(context) + (String) right.eval(context);
        }
        double l = ((Number) left.eval(context)).doubleValue();
        double r = ((Number) right.eval(context)).doubleValue();

        switch (name) {
            case '+': return l + r;
            case '-': return l - r;
            case '*': return l * r;
            case '/': return l / r;
            case '^': return Math.pow(l, r);
            default:
                throw new RuntimeException("Impossible");
        }
    }

    public int getPrecedence() {
        switch (name) {
            case '^':
                return Parser.PRECEDENCE_POWER;
            case '*':
            case '/':
                return Parser.PRECEDENCE_MULTIPLICATIVE;
            case '$':
            case '+':
            case '-':
                return Parser.PRECEDENCE_ADDITIVE;

            default:
                throw new RuntimeException("getPrecedence undefined for " + name);

        }
    }

    @Override
    public Expression resolve(Scope scope) {
        left = left.resolve(scope);
        right = right.resolve(scope);
        if (!left.getType().equals(right.getType())) {
            throw new RuntimeException("Argument types must match for operator '" + name + "'");
        }

        if (left.getType().equals(Type.STRING)) {
            if (name != '+') {
                throw new RuntimeException("Operator '" + name + "' not defined for strings");
            }
            name = '$';
        } else if (!left.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Arguments must be numbers" + (name == '+' ? " or strings" : "") + " for operator '" + name + "'");
        }
        return this;
    }

    @Override
    public Type getType() {
        return left.getType();
    }

    @Override
    public void toString(StringBuilder sb) {
        int precedence = getPrecedence();
        left.toString(sb, precedence);
        sb.append(' ');
        sb.append(name == '$' ? '+' : name);
        sb.append(' ');
        right.toString(sb, precedence);
    }
}
