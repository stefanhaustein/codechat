package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.type.Type;

public class InstanceReference extends Expression {
    public final int id;
    public final Type type;

    public InstanceReference(Type type, int id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.environment.getInstance(type, id, false);
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
