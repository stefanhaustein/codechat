package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.TupleType;

public abstract class Tuple {


    public abstract TupleType getType();

    public abstract Property getProperty(int index);

}
