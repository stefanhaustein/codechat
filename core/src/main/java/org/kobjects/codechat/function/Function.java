package org.kobjects.codechat.function;

import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Typed;

public interface Function extends Typed {

    EvaluationContext createContext();

    Object eval(EvaluationContext functionContext);

    FunctionType getType();
}
