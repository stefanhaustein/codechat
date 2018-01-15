package org.kobjects.codechat.annotation;

import javax.xml.soap.Text;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;

public class TextLink implements Link {

    private final CharSequence text;

    public TextLink(String s) {
        this(new AnnotatedString(s, null));
    }

    public TextLink(CharSequence text) {
        this.text = text;
    }

    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(text, EnvironmentListener.Channel.HELP);
    }
}
