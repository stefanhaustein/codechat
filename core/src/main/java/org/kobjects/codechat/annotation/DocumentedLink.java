package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Documentation;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;

public class DocumentedLink implements Link {
    private Object object;

    public DocumentedLink(Object object) {
        this.object = object;
    }


    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(Documentation.getDocumentation(environment, object), EnvironmentListener.Channel.HELP);
    }
}
