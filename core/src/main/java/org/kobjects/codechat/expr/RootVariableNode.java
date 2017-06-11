package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.type.Type;

public class RootVariableNode extends AbstractResolved {
    public String name;
    private Type type;

    RootVariableNode(String name, Type type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Object eval(EvaluationContext context) {
        return context.environment.rootVariables.get(name).value;
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
        sb.append(name);
    }

    @Override
    public int getChildCount() {
        return 0;
    }


    @Override
    public void assign(EvaluationContext context, Object value) {
        context.environment.rootVariables.get(name).value = value;
    }


    public boolean isAssignable() {
        return true;
    }
}
