package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.FunctionExpr;

public class Function {
    FunctionExpr definition;
    EvaluationContext contextTemplate;

    public Function(FunctionExpr definition, EvaluationContext contextTemplate) {
        this.definition = definition;
        this.contextTemplate = contextTemplate;
    }

    public EvaluationContext createContext() {
        return contextTemplate.clone();
    }

    public Object eval(EvaluationContext functionContext) {
        return definition.body.eval(functionContext);
    }

    public String toString() {
        return  definition.toString();
    }
}
