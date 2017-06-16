package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Typed;

public abstract class Tuple implements Typed {

    public abstract TupleType getType();

    public abstract Property getProperty(int index);

}
