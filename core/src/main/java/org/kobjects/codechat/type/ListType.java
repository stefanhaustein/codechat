package org.kobjects.codechat.type;

import java.util.List;

public class ListType extends CollectionType {
    public ListType(Type elementType) {
        super("List", elementType);
    }
}
