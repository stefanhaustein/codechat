package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;

public class UserMethod extends Method {
    private Statement body;
    final String[] parameterNames;

    public UserMethod(String name, FunctionType functionType, String[] parameterNames) {
        super(name, functionType);
        this.parameterNames = parameterNames;
    }

    public void setBody(Statement body) {
        this.body = body;
    }

    public Object eval(EvaluationContext functionContext) {
        return body.eval(functionContext);
    }



    public void toString(AnnotatedStringBuilder asb, int indent) {
        asb.indent(indent);
        asb.append(name);
        functionType.serializeSignature(asb, -1, null, parameterNames, null);
        asb.append(":\n");
        body.toString(asb,  indent + 2);
        asb.indent(indent);
        asb.append("end\n");
    }

}
