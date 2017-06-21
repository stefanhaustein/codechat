package org.kobjects.codechat.type;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public interface Instantiable<T extends Instance> {

    T createInstance(Environment environment, int id);


}
