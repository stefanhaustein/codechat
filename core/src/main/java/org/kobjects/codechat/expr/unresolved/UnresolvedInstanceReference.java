package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class UnresolvedInstanceReference extends UnresolvedExpression {
    public final int id;
    public final String typeName;

    public UnresolvedInstanceReference(int start, int end, String name) {
        super(start, end);
        int cut = name.indexOf('#');
        id = Integer.parseInt(name.substring(cut + 1));
;       typeName = name.substring(0, cut);
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        InstanceType type = parsingContext.environment.resolveInstanceType(typeName);
        if (type == null) {
            throw new ExpressionParser.ParsingException(start, end, "Can't resolve type '" + typeName + "'", null);
        }
        return new InstanceReference(type, id);
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append(typeName).append('#').append(id);
    }
}
