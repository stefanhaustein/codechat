package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;

public class ExecLink implements Link {

    private final String command;

    public ExecLink(String s) {
        command = s;
    }


    @Override
    public void execute(Environment environment) {
        environment.exec(command);
    }
}
