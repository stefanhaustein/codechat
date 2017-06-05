package org.kobjects.codechat.lang;

import java.util.Set;

public class SetType extends CollectionType {

    public SetType(Type elementType) {
        super(Set.class, elementType);
    }
}
