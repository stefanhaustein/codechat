package org.kobjects.codechat.lang;

public abstract class CollectionType extends Type {

    public final Type elementType;

    public CollectionType(Class javaType, Type elementType) {
        super(javaType);
        this.elementType = elementType;
    }

}
