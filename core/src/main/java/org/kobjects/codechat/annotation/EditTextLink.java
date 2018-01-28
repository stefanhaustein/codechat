package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;

public class EditTextLink implements Link {

    private String text;

    public EditTextLink(String s) {
        this.text = s;
    }

    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(text, EnvironmentListener.Channel.EDIT);
    }
}
