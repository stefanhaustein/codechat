package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class Reference extends Expression {
    int id;
    String typeName;
    Type type;

    public Reference(String name) {
        int cut = name.indexOf('#');
        id = Integer.parseInt(name.substring(cut + 1));
;       typeName = name.substring(0, cut);
    }

    @Override
    public Object eval(Context context) {
        return context.environment.getInstance(type, id);
    }

    @Override
    public Expression resolve(Scope scope) {
        type = scope.environment.resolveType(typeName);
        if (type == null) {
            throw new RuntimeException("Can't resolve type '" + typeName + "'");
        }
        return this;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(type).append('#').append(id);
    }
}
