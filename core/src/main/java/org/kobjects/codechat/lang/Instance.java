package org.kobjects.codechat.lang;


import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Typed;

public interface Instance extends HasDependencies, Typed, Entity, Printable {

    Property getProperty(int index);

    boolean needsTwoPhaseSerilaization();

}
