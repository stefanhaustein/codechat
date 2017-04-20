package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.Context;
import org.kobjects.codechat.lang.Scope;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.lang.Variable;

public class VariableNode extends Resolved {
    Variable variable;

    public VariableNode(Variable variable) {
        this.variable = variable;
    }


    @Override
    public Object eval(Context context) {
        return context.variables[variable.getIndex()];
    }

    @Override
    public void assign(Context context, Object value) {
        context.variables[variable.getIndex()] = value;
    }

    @Override
    public Type getType() {
        return variable.getType();
    }

    @Override
    public void toString(StringBuilder sb, int parentPrecedence) {
        sb.append(variable.getName());
    }
}
