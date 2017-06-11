package org.kobjects.codechat.type;

public abstract class CollectionType extends Type {

    public final Type elementType;

    public CollectionType(Type elementType) {
        this.elementType = elementType;
    }

}
