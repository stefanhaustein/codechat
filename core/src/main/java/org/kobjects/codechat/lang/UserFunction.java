package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.type.FunctionType;

public class UserFunction implements Function {
    private FunctionExpression definition;
    private EvaluationContext contextTemplate;

    public UserFunction(FunctionExpression definition, EvaluationContext contextTemplate) {
        this.definition = definition;
        this.contextTemplate = contextTemplate;
    }

    public boolean isNamed() {
        return definition.name != null;
    }

    public EvaluationContext createContext() {
        return contextTemplate.clone();
    }

    public Object eval(EvaluationContext functionContext) {
        return definition.body.eval(functionContext);
    }

    @Override
    public FunctionType getType() {
        return definition.getType();
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

    public void serializeSignature(StringBuilder sb) {
        definition.serializeSignature(sb);
    }
}
