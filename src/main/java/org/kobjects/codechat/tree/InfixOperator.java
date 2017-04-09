package org.kobjects.codechat.tree;

import org.kobjects.codechat.Environment;

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

    private double evalNumber(double p1, double p2) {
        switch (name) {
            case "+": return p1 + p2;
            case "-": return p1 - p2;
            case "*": return p1 * p2;
            case "/": return p1 / p2;
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

    public String toString() {
        if (right == null) {
            return "(" + name + "(" + left + "))";
        }
        return "((" + left + ") " + name + " (" + right + "))";
    }

}
