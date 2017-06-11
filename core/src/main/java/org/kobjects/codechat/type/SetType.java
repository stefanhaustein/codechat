package org.kobjects.codechat.type;

import java.util.Set;

public class SetType extends CollectionType {

    public SetType(Type elementType) {
        super(elementType);
    }

    @Override
    public String getName() {
        return "set<" + elementType + ">";
    }

    @Override
    public Class<?> getJavaClass() {
        return Set.class;
    }
}
