package org.kobjects.codechat.expr;

import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Instance;

public class InstanceRef extends Node {
    int id;
    String type;

    public InstanceRef(Node left, Node right) {
        if (!(left instanceof Identifier)) {
            throw new RuntimeException("Left argument must be identifier.");
        }
        if (!(right instanceof Literal) || !(((Literal) right).value instanceof Number)) {
            throw new IllegalArgumentException("Right argument must be number");
        }
        type = ((Identifier) left).name;
        id = ((Number) ((Literal) right).value).intValue();
    }

    @Override
    public Object eval(Environment environment) {
        return environment.getInstance(type, id);
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(type).append('#').append(id);
    }
}
