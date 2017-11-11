package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public abstract class NativeFunction implements Function {
    private final FunctionType type;

    public NativeFunction(Type returnType, Type... parameterTypes) {
        this.type = new FunctionType(returnType, parameterTypes);
    }

    @Override
    public EvaluationContext createContext() {
        return new EvaluationContext(null, type.parameterTypes.length);
    }

    @Override
    public Object eval(EvaluationContext functionContext) {
        return eval(functionContext.variables);
    }

    protected abstract Object eval(Object[] params);

    @Override
    public FunctionType getType() {
        return type;
    }

}
