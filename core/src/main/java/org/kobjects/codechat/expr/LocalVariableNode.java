package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Parser;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.lang.LocalVariable;

public class LocalVariableNode extends AbstractResolved {
    public LocalVariable variable;

    public LocalVariableNode(LocalVariable variable) {
        this.variable = variable;
    }


    @Override
    public Object eval(EvaluationContext context) {
        return context.variables[variable.getIndex()];
    }

    @Override
    public void assign(EvaluationContext context, Object value) {
        context.variables[variable.getIndex()] = value;
    }

    @Override
    public Type getType() {
        return variable.getType();
    }

    @Override
    public int getPrecedence() {
        return Parser.PRECEDENCE_PATH;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(variable.getName());
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    public boolean isAssignable() {
        return true;
    }
}
