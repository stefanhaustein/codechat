package org.kobjects.codechat.lang;


public class EvaluationContext {
    public final Environment environment;
    public final Object[] variables;

    EvaluationContext(Environment environment, int varCount) {
        this.environment = environment;
        this.variables = new Object[varCount];
    }

    EvaluationContext(Environment environment, Object[] template) {
        this(environment, template.length);
        System.arraycopy(template, 0, variables, 0, template.length);
    }
}
