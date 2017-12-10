package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UserFunction extends Instance implements Function {
    private EvaluationContext contextTemplate;
    private int id;
    private FunctionType functionType;
    private Statement body;
    private Closure closure;
    private String[] parameterNames;

    public UserFunction(Environment environment, FunctionType functionType, int id) {
        super(environment, id);
        this.functionType = functionType;
    }

    public void init(FunctionExpression definition, EvaluationContext contextTemplate) {
        this.body = definition.body;
        this.closure = definition.closure;
        this.parameterNames = definition.parameterNames;
        this.contextTemplate = contextTemplate;
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


    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.setSerialized(this);

        boolean wrap = closure.toString(asb.getStringBuilder(), contextTemplate);
        int indent = wrap ? 1 : 0;

        functionType.serializeSignature(asb, id, serializationContext.getEnvironment().constants.get(this), parameterNames, new EntityLink(this));

        if (serializationContext.getMode() == SerializationContext.Mode.LIST) {
            asb.append("\n");
        } else {
            asb.append(":\n");
            body.toString(asb, indent + 1);
            AbstractStatement.indent(asb, indent);
            asb.append("end\n");

            if (wrap) {
                asb.append("end\n");
            }
        }
    }

    @Override
    public Property getProperty(int index) {
        return null;
    }

    @Override
    public void delete() {

    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (body != null) {
            body.getDependencies(result);
        }
    }
}
