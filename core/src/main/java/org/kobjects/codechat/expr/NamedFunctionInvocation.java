package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.type.Type;

public class NamedFunctionInvocation extends AbstractResolved {
    String name;
    Function function;
    boolean parens;
    Expression[] parameters;

    public NamedFunctionInvocation(String name, Function funciton, boolean parens, Expression... parameters) {
        this.name = name;
        this.function = funciton;
        this.parens = parens;
        this.parameters = parameters;
    }

    @Override
    public Object eval(EvaluationContext context) {
        EvaluationContext functionContext = function.createContext();
        for (int i = 0; i < parameters.length; i++) {
            functionContext.variables[i] = parameters[i].eval(context);
        }
        return function.eval(functionContext);
    }

    @Override
    public Type getType() {
        return function.getType().returnType;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {
        sb.append(name);
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
        return parameters.length;
    }

    @Override
    public Expression getChild(int index) {
        return parameters[index];
    }
}
