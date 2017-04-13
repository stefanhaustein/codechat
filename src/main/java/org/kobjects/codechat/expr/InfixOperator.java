package org.kobjects.codechat.expr;

import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Processor;

public class InfixOperator extends Node {

    final String name;
    Node left;
    Node right;
    public InfixOperator(String name, Node left, Node right) {
        this.name = name;
        this.left = left;
        this.right = right;
    }

    @Override
    public Object eval(Environment environment) {
        if (right == null) {
            Object p0 = left.eval(environment);
            if (p0 instanceof Number) {
                return evalNumber(((Number) p0).doubleValue());
            }
            throw new RuntimeException("Can't apply " + name + " to " + p0.getClass().getSimpleName());
        } else if (name.equals("=")) {
            Object value = right.eval(environment);
            left.assign(environment, value);
            return value;
        } else {
            Object p0 = left.eval(environment);
            Object p1 = right.eval(environment);
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
                return Processor.PRECEDENCE_POWER;
            case "*":
            case "/":
                return Processor.PRECEDENCE_MULTIPLICATIVE;
            case "+":
            case "-":
                return right == null ? Processor.PRECEDENCE_SIGN : Processor.PRECEDENCE_ADDITIVE;
            case "<":
            case "<=":
            case ">":
            case ">=":
                return Processor.PRECEDENCE_RELATIONAL;
            case "=":
                return Processor.PRECEDENCE_EQUALITY;
            default:
                throw new RuntimeException("getPrecedence undefined for " + name);

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
