package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;

public class InstanceReference extends Expression {
    int id;
    String typeName;
    Type type;

    public InstanceReference(String name) {
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
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb) {
        sb.append(type).append('#').append(id);
    }
}
