package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.ConstructorInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class UnresolvedConstructor extends UnresolvedExpression {

    public final String typeName;
    public final int id;

    public UnresolvedConstructor(int start, int end, String typeName, int id) {
        super(start, end);
        this.typeName = typeName;
        this.id = id;
    }

    @Override
    public Expression resolve(ParsingContext parsingContext, Type expectedType) {
        Type type = parsingContext.environment.resolveType(typeName);
        return new ConstructorInvocation((InstanceType) type, id);
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append("new ").append(typeName);
        if (id != -1) {
            sb.append('#').append(id);
        }
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }
}
