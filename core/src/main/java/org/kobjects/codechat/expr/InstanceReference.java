package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

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
    public Object eval(EvaluationContext context) {
        return context.environment.getInstance(type, id, false);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        type = parsingContext.environment.resolveType(typeName);
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
    public void toString(StringBuilder sb, int indent) {
        sb.append(type).append('#').append(id);
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
