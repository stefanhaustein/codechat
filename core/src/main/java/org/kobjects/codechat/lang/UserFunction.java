package org.kobjects.codechat.lang;

import java.util.Collection;
import java.util.Map;

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
    public void serialize(AnnotatedStringBuilder asb, Detail detail, Map<Dependency, Environment.SerializationState> serializationStateMap) {
        serializeWithName(asb, detail, serializationStateMap, null);
    }

    public void serializeWithName(AnnotatedStringBuilder asb, Detail detail, Map<Dependency, Environment.SerializationState> serializationStateMap, String name) {
        int start = asb.length();
        int nameEnd = -1;
        if (detail == Detail.DECLARATION) {
            nameEnd = functionType.serializeSignature(asb.getStringBuilder(), id, name, parameterNames, null);
            asb.append(";\n");
        } else if (body != null) {
            boolean wrap = closure.toString(asb.getStringBuilder(), contextTemplate);
            int indent = wrap ? 1 : 0;

            start = asb.length();
            nameEnd = functionType.serializeSignature(asb.getStringBuilder(), id, name, parameterNames, null);

            asb.append(":\n");
            body.toString(asb.getStringBuilder(), indent + 1);
            AbstractStatement.indent(asb.getStringBuilder(), indent);
            asb.append("end;\n");

            if (wrap) {
                asb.append("end;\n");
            }
        }
        if (nameEnd != -1 && asb.getAnnotationList() != null) {
            asb.getAnnotationList().add(new Annotation(start, nameEnd, this));
        }
    }

    @Override
    public void delete() {

    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        if (body != null) {
            body.getDependencies(environment, result);
        }
    }
}
