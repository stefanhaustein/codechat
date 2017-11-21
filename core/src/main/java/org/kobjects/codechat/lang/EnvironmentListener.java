package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.annotation.AnnotationSpan;

public interface EnvironmentListener {
    void clearAll();

    void paused(boolean paused);

    void setName(String name);

    void print(CharSequence s);

    void showError(CharSequence s);

    void edit(String s);
}
