package org.kobjects.codechat.type;

import java.util.List;

public class ArrayType extends CollectionType {
    public ArrayType(Type elementType) {
        super(elementType);
    }

    @Override
    public String getName() {
        return "array[" + elementType + "]";
    }

    @Override
    public Class<?> getJavaClass() {
        return List.class;
    }
}
