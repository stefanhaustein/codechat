package org.kobjects.codechat.lang;


public class Context {
    public Environment environment;
    public Object[] variables;

    Context(Environment environment) {
        this.environment = environment;
    }
}
