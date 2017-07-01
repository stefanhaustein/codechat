package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Typed;

public interface Function extends Typed {

    EvaluationContext createContext();

    Object eval(EvaluationContext functionContext);

    FunctionType getType();
}
