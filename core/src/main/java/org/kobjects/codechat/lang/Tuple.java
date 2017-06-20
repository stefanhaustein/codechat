package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Typed;

public interface Tuple extends Typed {
    TupleType getType();

    Property getProperty(int index);

}
