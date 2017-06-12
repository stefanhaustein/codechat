package org.kobjects.codechat.lang;


public class EvaluationContext {
    public final Environment environment;
    public final Object[] variables;

    EvaluationContext(Environment environment, int varCount) {
        this.environment = environment;
        this.variables = new Object[varCount];
    }

    public EvaluationContext clone() {
        EvaluationContext result = new EvaluationContext(environment, variables.length);
        System.arraycopy(variables, 0, result.variables, 0, variables.length);
        return result;
    }

}
