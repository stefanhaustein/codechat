package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.statement.AbstractStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UserFunction implements Function, Instance {
    private EvaluationContext contextTemplate;
    private int id;
    private FunctionType functionType;
    private Statement body;
    private Closure closure;
    private String[] parameterNames;

    public UserFunction(FunctionType functionType, int id) {
        this.functionType = functionType;
        this.id = id;
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
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public void serializeStub(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        functionType.serializeSignature(asb, id, serializationContext.getEnvironment().constants.get(this), parameterNames, new EntityLink(this));
        asb.append(" : fwd;\n");
        serializationContext.setState(this, SerializationContext.SerializationState.STUB_SERIALIZED);
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        serializationContext.serializeDependencies(asb,this);
        serializationContext.setState(this, SerializationContext.SerializationState.STUB_SERIALIZED);

        boolean wrap = closure.toString(asb.getStringBuilder(), contextTemplate);
        int indent = wrap ? 1 : 0;

        functionType.serializeSignature(asb, id, serializationContext.getEnvironment().constants.get(this), parameterNames, new EntityLink(this));

        asb.append(":\n");
        body.toString(asb.getStringBuilder(), indent + 1);
        AbstractStatement.indent(asb.getStringBuilder(), indent);
        asb.append("end;\n");

        if (wrap) {
            asb.append("end;\n");
        }
        serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
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
