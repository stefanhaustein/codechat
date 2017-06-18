package org.kobjects.codechat.type;

public abstract class CollectionType extends Type {

    public final Type elementType;

    public CollectionType(Type elementType) {
        this.elementType = elementType;
    }

    public boolean isAssignableFrom(Type other) {
        if (!(other instanceof CollectionType)) {
            return false;
        }
        CollectionType otherType = (CollectionType) other;
        return otherType.getClass() == getClass() && elementType.isAssignableFrom(otherType.elementType);
    }

}
