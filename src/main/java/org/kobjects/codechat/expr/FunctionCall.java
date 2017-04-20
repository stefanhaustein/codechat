package org.kobjects.codechat.expr;

import java.util.List;
import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class FunctionCall extends Expression {
    private final String name;
    private Expression[] children;
    public FunctionCall(String identifier, List<Expression> arguments) {
        this.name = identifier;
        children = arguments.toArray(new Expression[arguments.size()]);
    }

    @Override
    public Expression resolve(Scope scope) {
        for (int i = 0; i < children.length; i++) {
            children[i] = children[i].resolve(scope);
        }
        return this;
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(name);
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
    public Object eval(Context context) {
        if (children.length == 1) {
            Object p0 = children[0].eval(context);
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
