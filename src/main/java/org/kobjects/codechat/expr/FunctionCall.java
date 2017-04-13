package org.kobjects.codechat.expr;

import java.util.List;
import org.kobjects.codechat.Environment;

public class FunctionCall extends Node {
    private final String name;
    private Node[] children;
    public FunctionCall(String identifier, List<Node> arguments) {
        this.name = identifier;
        children = arguments.toArray(new Node[arguments.size()]);
    }

    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append("(");
        if (children.length > 0) {
            sb.append(children[0]);
            for (int i = 1; i < children.length; i++) {
                sb.append(", ");
                children[i].toString(sb, 0);
            }
        }
        sb.append(")");
    }

    @Override
    public Object eval(Environment environment) {
        if (children.length == 1) {
            Object p0 = children[0].eval(environment);
            if (p0 instanceof Number) {
               return evalNumber(((Number) p0).doubleValue());
            }
        }
        throw new RuntimeException("Undefined function " + name);
    }

    public double evalNumber(double p) {
        switch (name) {
            case "abs": return Math.abs(p);
            case "acos": return Math.acos(p);
            case "asin": return Math.asin(p);
            case "atan": return Math.atan(p);
            case "cos": return Math.cos(p);
            case "cosh": return Math.cosh(p);
            case "sin": return Math.sin(p);
            case "sinh": return Math.sinh(p);
            case "tan": return Math.tan(p);
            case "tanh": return Math.tanh(p);
            default:
                throw new RuntimeException("Function " + name + " not defined for number parameter.");

        }
    }
}
