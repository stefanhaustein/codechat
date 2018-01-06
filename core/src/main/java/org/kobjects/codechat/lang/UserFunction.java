package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UserFunction extends AbstractInstance implements Function {
    private EvaluationContext contextTemplate;
    private FunctionType functionType;
    private Statement body;
    private Closure closure;
    private String[] parameterNames;

    public UserFunction(Environment environment, FunctionType functionType) {
        super(environment);
        this.functionType = functionType;
    }

    public void init(Statement body, Closure closure, String[] parameterNames, EvaluationContext contextTemplate) {
        this.body = body;
        this.closure = closure;
        this.parameterNames = parameterNames;
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
    public void print(AnnotatedStringBuilder asb, Flavor flavor) {
        boolean wrap = closure.toString(asb.getStringBuilder(), contextTemplate);
        int indent = wrap ? 2 : 0;

        String name = environment.getConstantName(this);
        functionType.serializeSignature(asb, name == null ? environment.getId(this) : -1, name, parameterNames, new InstanceLink(this));

        if (flavor == Printable.Flavor.LIST) {
            asb.append("\n");
        } else {
            asb.append(":\n");
            body.toString(asb, indent + 2);
            asb.indent(indent);
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
    public void getDependencies(DependencyCollector result) {
        if (body != null) {
            body.getDependencies(result);
        }
    }
}
