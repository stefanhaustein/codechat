package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class FunctionInvocation extends Expression {

    Expression base;
    boolean parens;
    Expression[] parameters;

    public FunctionInvocation(Expression base, boolean parens, Expression... parameters) {
        this.base = base;
        this.parens = parens;
        this.parameters = parameters;
    }

    @Override
    public Object eval(EvaluationContext context) {
        Function f = (Function) base.eval(context);
        EvaluationContext functionContext = f.createContext();
        for (int i = 0; i < parameters.length; i++) {
            functionContext.variables[i] = parameters[i].eval(context);
        }
        return f.eval(functionContext);
    }

    @Override
    public Type getType() {
        return ((FunctionType) base.getType()).returnType;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        base.toString(sb, indent);
        sb.append(parens ? '(' : ' ');
        if (parameters.length > 0) {
            parameters[0].toString(sb, indent);
            for (int i = 1; i < parameters.length; i++) {
                sb.append(", ");
                parameters[i].toString(sb, indent);
            }
        }
        if (parens) {
            sb.append(")");
        }
    }

    @Override
    public int getChildCount() {
        return parameters.length + 1;
    }

    @Override
    public Expression getChild(int index) {
        return index == 0 ? base : parameters[index - 1];
    }

}
