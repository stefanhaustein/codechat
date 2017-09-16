package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.type.Type;

public class ConstructorInvocation extends Expression {

    Type type;
    int id;

    public ConstructorInvocation(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.environment.instantiate(type, id);
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
        sb.append("new ").append(type.toString());
    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
