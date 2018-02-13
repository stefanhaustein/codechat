package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.type.FunctionType;

public abstract class Method {
    public final String name;
    final FunctionType functionType;

    public Method(String name, FunctionType functionType) {
        this.name = name;
        this.functionType = functionType;
    }


    public abstract Object eval(EvaluationContext functionContext);

    public FunctionType getType() {
        return functionType;
    }


    public void toString(AnnotatedStringBuilder asb, int indent) {
        asb.indent(indent);
        asb.append(name);
        functionType.serializeSignature(asb, -1, null, null, null);
    }

}
