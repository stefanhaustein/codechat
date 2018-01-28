package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;

public class ErrorLink implements Link {
    private final String message;
    public ErrorLink(Exception exception) {
        this.message = exception.getMessage();
    }


    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(message, EnvironmentListener.Channel.ERROR);
    }
}
