package org.kobjects.codechat.lang;


import org.kobjects.codechat.instance.Instance;

public class EvaluationContext {
    public final Environment environment;
    public final Object[] variables;
    public Instance self;

    public EvaluationContext(Environment environment, int varCount) {
        this.environment = environment;
        this.variables = new Object[varCount];
        this.self = null;
    }

    public EvaluationContext(Environment environment, int varCount, Instance self) {
        this.environment = environment;
        this.variables = new Object[varCount];
        this.self = self;
    }

    public EvaluationContext clone() {
        EvaluationContext result = new EvaluationContext(environment, variables.length);
        System.arraycopy(variables, 0, result.variables, 0, variables.length);
        return result;
    }

}
