package org.kobjects.codechat.type;

import java.util.List;

public class ListType extends CollectionType {
    public ListType(Type elementType) {
        super(elementType);
    }

    @Override
    public String getName() {
        return "list<" + elementType + ">";
    }

    @Override
    public Class<?> getJavaClass() {
        return List.class;
    }
}
