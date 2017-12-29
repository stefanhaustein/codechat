package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;

public class DocumentedLink implements Link {
    private Documented documented;

    public DocumentedLink(Documented documented) {
        this.documented = documented;
    }


    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(Formatting.getDocumentation(documented));
    }
}
