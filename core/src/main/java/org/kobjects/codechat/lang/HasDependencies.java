package org.kobjects.codechat.lang;

import java.util.Collection;

public interface HasDependencies {

    void getDependencies(Environment environment, Collection<Entity> result);

}
