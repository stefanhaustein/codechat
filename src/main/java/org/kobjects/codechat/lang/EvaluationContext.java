package org.kobjects.codechat.lang;


public class EvaluationContext {
    public Environment environment;
    public Object[] variables;

    EvaluationContext(Environment environment) {
        this.environment = environment;
    }
}
