package org.kobjects.codechat.lang;


public class EvaluationContext {
    public final Environment environment;
    public final Object[] variables;

    EvaluationContext(Environment environment, int varCount) {
        this.environment = environment;
        this.variables = new Object[varCount];
    }
}
