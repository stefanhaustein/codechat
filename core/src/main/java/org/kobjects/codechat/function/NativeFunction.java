package org.kobjects.codechat.function;

import org.kobjects.codechat.lang.EvaluationContext;
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
