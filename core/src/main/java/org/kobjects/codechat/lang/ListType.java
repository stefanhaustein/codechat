package org.kobjects.codechat.lang;

import java.util.List;

public class ListType extends CollectionType {
    public ListType(Type elementType) {
        super(List.class, elementType);
    }
}
