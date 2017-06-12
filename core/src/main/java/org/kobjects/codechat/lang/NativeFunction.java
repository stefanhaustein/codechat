package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.FunctionType;

public abstract class NativeFunction implements Function {

    FunctionType type;

    public NativeFunction(FunctionType functionType) {
        this.type = type;
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
}
