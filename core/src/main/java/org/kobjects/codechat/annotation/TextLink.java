package org.kobjects.codechat.annotation;

import javax.xml.soap.Text;
import org.kobjects.codechat.lang.Environment;

public class TextLink implements Link {

    AnnotatedCharSequence text;

    public TextLink(String s) {
        this.text = new AnnotatedString(s, null);
    }

    public TextLink(AnnotatedCharSequence text) {
        this.text = text;
    }

    @Override
    public void execute(Environment environment) {
        environment.environmentListener.print(text);
    }
}
