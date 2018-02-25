package org.kobjects.codechat.instance;


import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.HasDependencies;
import org.kobjects.codechat.lang.Printable;
import org.kobjects.codechat.type.Typed;

public interface Instance extends HasDependencies, Typed, Printable {

    Property getProperty(int index);

    boolean needsTwoPhaseSerilaization();

    Environment getEnvironment();

    void delete();
}
