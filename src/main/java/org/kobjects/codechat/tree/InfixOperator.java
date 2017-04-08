package org.kobjects.codechat.tree;

import org.kobjects.codechat.Environment;

public class InfixOperator extends Node {

    final String name;
    public InfixOperator(String name, Node... children) {
        super(children);
        this.name = name;
    }

    @Override
    public Object eval(Environment environment) {
        if (children.length == 1) {
            Object p0 = children[0].eval(environment);
            if (p0 instanceof Number) {
                return evalNumber(((Number) p0).doubleValue());
            }
            throw new RuntimeException("Can't apply " + name + " to " + p0.getClass().getSimpleName());
        } else if (name.equals("=")) {
            Object value = children[1].eval(environment);
            children[0].assign(environment, value);
            return value;
        } else {
            Object p0 = children[0].eval(environment);
            Object p1 = children[1].eval(environment);
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
        if (children.length == 1) {
            return "(" + name + "(" + children[0] + "))";
        }
        return "((" + children[0] + ") " + name + " (" + children[1] + "))";
    }

}
