package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotationSpan;

public interface EnvironmentListener {
    enum Channel {
        OUTPUT, HELP, ERROR, EDIT
    }


    void clearAll();

    void suspended(boolean paused);

    void setName(String name);

    void print(CharSequence s, Channel channel);

    void loadExample(String name);
}
