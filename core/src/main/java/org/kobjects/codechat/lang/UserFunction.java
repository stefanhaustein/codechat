package org.kobjects.codechat.lang;

import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UserFunction implements Function {
    private EvaluationContext contextTemplate;
    private int id;
    private FunctionType functionType;
    private String name;
    private Statement body;
    private Closure closure;
    private String[] parameterNames;

    public UserFunction(FunctionType functionType, int id) {
        this.functionType = functionType;
        this.id = id;
    }

    public void init(FunctionExpression definition, EvaluationContext contextTemplate) {
        this.name = definition.name;
        this.body = definition.body;
        this.closure = definition.closure;
        this.parameterNames = definition.parameterNames;
        this.contextTemplate = contextTemplate;
    }

    public boolean isNamed() {
        return name != null;
    }

    public EvaluationContext createContext() {
        return contextTemplate.clone();
    }

    public Object eval(EvaluationContext functionContext) {
        return body.eval(functionContext);
    }

    @Override
    public FunctionType getType() {
        return functionType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean wrap = closure.toString(sb, contextTemplate);
        int indent = wrap ? 1 : 0;

        functionType.serializeSignature(sb, id, name, parameterNames);
        sb.append(" {\n");
        body.toString(sb, indent + 1);
        AbstractStatement.indent(sb, indent);
        sb.append("}\n");

        if (wrap) {
            sb.append("}\n");
        }
        return sb.toString();
    }

    public void serializeSignature(StringBuilder sb) {
        functionType.serializeSignature(sb, id, name, parameterNames);
    }
}
