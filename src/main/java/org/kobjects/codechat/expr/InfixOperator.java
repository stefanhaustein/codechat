package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class InfixOperator extends Expression {

    public final String name;
    public Expression left;
    public Expression right;
    public InfixOperator(String name, Expression left, Expression right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(Context context) {
        Object p0 = left.eval(context);
        if (right == null) {
            if (p0 instanceof Number) {
                return evalNumber(((Number) p0).doubleValue());
            }
            throw new RuntimeException("Can't apply " + name + " to " + p0.getClass().getSimpleName());
        }
        Object p1 = right.eval(context);
        if (p0.getClass() != p1.getClass()) {
           throw new RuntimeException("Parameters must be of the same type for " + name);
        }
        if (p0 instanceof Number) {
           return evalNumber(((Number) p0).doubleValue(), ((Number) p1).doubleValue());
        }
        if (p0 instanceof String) {
            return evalString((String) p0, (String) p1);
        }
        throw new RuntimeException("Can't apply " + name + " to " + p0.getClass().getSimpleName());
    }

    private Object evalNumber(double p1, double p2) {
        switch (name) {
            case "+": return p1 + p2;
            case "-": return p1 - p2;
            case "*": return p1 * p2;
            case "/": return p1 / p2;
            case ">": return p1 > p2;
            case ">=": return p1 >= p2;
            case "==": return p1 == p2;
            case "<": return p1 < p2;
            case "<=": return p1 <= p2;
            default:
                throw new RuntimeException("Can't apply "+ name + " to numbers.");
        }
    }

    private String evalString(String p1, String p2) {
        switch (name) {
            case "+": return p1 + p2;
            default:
                throw new RuntimeException("Can't apply " + name + " to strings.");
        }
    }

    private double evalNumber(double param) {
        switch (name) {
            case "-": return -param;
            default:
                throw new RuntimeException("Can't apply "+ name + " to number.");
        }
    }

    private int getPrecedence() {
        switch (name) {
            case "^":
                return Parser.PRECEDENCE_POWER;
            case "*":
            case "/":
                return Parser.PRECEDENCE_MULTIPLICATIVE;
            case "+":
            case "-":
                return right == null ? Parser.PRECEDENCE_SIGN : Parser.PRECEDENCE_ADDITIVE;
            case "<":
            case "<=":
            case ">":
            case ">=":
                return Parser.PRECEDENCE_RELATIONAL;
            case "=":
                return Parser.PRECEDENCE_EQUALITY;
            default:
                throw new RuntimeException("getPrecedence undefined for " + name);

        }
    }

    @Override
    public Expression resolve(Scope scope) {
        left = left.resolve(scope);
        if (right != null) {
            right = right.resolve(scope);
            if (name.equals("=")) {
                return new Assignment(left, right);
            }
        }
        return this;
    }

    @Override
    public Type getType() {
        switch (name) {
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "=":
            case "==":
                return Type.BOOLEAN;
            default:
                return left.getType();
        }
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        int precedence = getPrecedence();
        boolean braces = parentPrecedence > precedence;
        if (braces) {
            sb.append('(');
        }
        if (right == null) {
            sb.append(name);
            left.toString(sb, precedence);
        } else {
            left.toString(sb, precedence);
            sb.append(' ');
            sb.append(name);
            sb.append(' ');
            right.toString(sb, precedence);
        }
        if (braces) {
            sb.append(')');
        }
    }
}
