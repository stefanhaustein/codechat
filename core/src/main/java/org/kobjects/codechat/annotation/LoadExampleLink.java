package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;

public class LoadExampleLink implements Link {

    private final String name;

    public LoadExampleLink(String name) {
        this.name = name;
    }

    @Override
    public void execute(Environment environment) {
        environment.environmentListener.loadExample(name);
    }
}
