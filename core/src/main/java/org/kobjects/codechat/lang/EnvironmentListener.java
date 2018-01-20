package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotationSpan;

public interface EnvironmentListener {
    enum Channel {
        OUTPUT, HELP
    }


    void clearAll();

    void suspended(boolean paused);

    void setName(String name);

    void print(CharSequence s, Channel channel);

    void showError(CharSequence s);

    void edit(String s);

    void loadExample(String name);
}
