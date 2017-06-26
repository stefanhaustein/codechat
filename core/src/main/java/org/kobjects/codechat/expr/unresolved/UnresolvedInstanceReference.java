package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class UnresolvedInstanceReference extends UnresolvedExpression {
    int id;
    String typeName;

    public UnresolvedInstanceReference(String name) {
        int cut = name.indexOf('#');
        id = Integer.parseInt(name.substring(cut + 1));
;       typeName = name.substring(0, cut);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext) {
        Type type = parsingContext.environment.resolveType(typeName);
        if (type == null) {
            throw new RuntimeException("Can't resolve type '" + typeName + "'");
        }
        return new InstanceReference(type, id);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(typeName).append('#').append(id);
    }
}
