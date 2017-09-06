package org.kobjects.codechat.annotation;

import org.kobjects.codechat.lang.Environment;

public class EditTextLink implements Link {

    private String text;

    public EditTextLink(String s) {
        this.text = s;
    }

    @Override
    public void execute(Environment environment) {
        environment.environmentListener.edit(text);
    }
}
