package org.kobjects.codechat.expr;

import org.kobjects.codechat.Environment;
import org.kobjects.codechat.Instance;

public class InstanceRef extends Node {
    int id;
    String type;

    public InstanceRef(String name) {
        int cut = name.indexOf('#');
        id = Integer.parseInt(name.substring(cut + 1));
;       type = name.substring(0, cut);
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
