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
        StringBuilder sb = new StringBuilder();
        boolean wrap = definition.closure.toString(sb, contextTemplate);
        definition.toString(sb, wrap ? 1 : 0);
        if (wrap) {
            sb.append("}\n");
        }
        return sb.toString();
    }
}
