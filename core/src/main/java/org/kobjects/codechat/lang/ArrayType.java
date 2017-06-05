package org.kobjects.codechat.lang;

import java.util.List;

public class ArrayType extends CollectionType {
    public ArrayType(Type elementType) {
        super(List.class, elementType);
    }
}
