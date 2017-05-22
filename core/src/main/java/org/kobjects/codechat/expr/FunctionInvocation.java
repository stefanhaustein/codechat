package org.kobjects.codechat.expr;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;

public class FunctionInvocation extends AbstractResolved {

    Expression base;
    Expression[] parameters;

    public FunctionInvocation(Expression base, Expression[] parameters) {
        this.base = base;
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
        return null;
    }

    @Override
    public int getPrecedence() {
        return 0;
    }

    @Override
    public void toString(StringBuilder sb, int indent) {

    }

    @Override
    public int getChildCount() {
        return 0;
    }
}
