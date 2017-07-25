package org.kobjects.codechat.lang;

import java.util.List;
import org.kobjects.codechat.type.Typed;

/**
 * Note that instances are not necessarily (constructor-) instantiable
 */
public interface Instance extends Typed,Dependency {

    enum Detail {
        DECLARATION, // new Foo#31
        DEFINITION,  // Foo#31{bar: "x"}
        DETAIL       // Foo#31.bar = "x"
    }

    int getId();
    void serialize(AnnotatedStringBuilder asb, Detail detail);
    void delete();
}
