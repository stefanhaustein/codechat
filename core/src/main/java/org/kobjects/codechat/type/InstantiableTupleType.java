package org.kobjects.codechat.type;

import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.Tuple;

public abstract class InstantiableTupleType<T extends Instance> extends TupleType implements Instantiable<T> {
    public InstantiableTupleType(String name, Class<? extends Tuple> javaClass) {
        super(name, javaClass);
    }
}
