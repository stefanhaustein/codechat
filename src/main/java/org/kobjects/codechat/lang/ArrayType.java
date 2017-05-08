package org.kobjects.codechat.lang;

import java.util.List;

public class ArrayType extends Type {

    public final Type elementType;

    private ArrayType(Type elementType) {
        super(List.class);
        this.elementType = elementType;
    }
}
