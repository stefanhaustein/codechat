package org.kobjects.codechat.type;

public class ListType extends CollectionType {
    public ListType(Type elementType) {
        super("List", elementType);
    }
}
