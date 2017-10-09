package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.Typed;

/**
 * Note that instances are not necessarily (constructor-) instantiable
 */
public interface Instance extends Typed, Entity, HasDependencies {

    int getId();

    void setId(int id);

    void delete();
}
