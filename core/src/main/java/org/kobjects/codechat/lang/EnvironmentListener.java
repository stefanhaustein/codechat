package org.kobjects.codechat.lang;

import java.util.List;

public interface EnvironmentListener {
    void paused(boolean paused);

    void setName(String name);

    void print(String s, List<Annotation> annotations);
}
