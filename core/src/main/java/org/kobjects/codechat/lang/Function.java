package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.FunctionType;

public interface Function {

    EvaluationContext createContext();

    Object eval(EvaluationContext functionContext);

    FunctionType getType();
}
