package org.kobjects.codechat.lang;

public interface Dependency {
    void serialize(AnnotatedStringBuilder asb, Instance.Detail detail);
}
