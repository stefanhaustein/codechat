package org.kobjects.codechat.lang;

public interface Function {

    EvaluationContext createContext();

    Object eval(EvaluationContext functionContext);
}
