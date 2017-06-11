package org.kobjects.codechat.type;

import java.util.Set;

public class SetType extends CollectionType {

    public SetType(Type elementType) {
        super(Set.class, elementType);
    }
}
