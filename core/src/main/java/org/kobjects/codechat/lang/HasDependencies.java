package org.kobjects.codechat.lang;

import java.util.Collection;
import javax.naming.Context;

public interface HasDependencies {

    void getDependencies(Environment environment, Collection<Dependency> result);

}
