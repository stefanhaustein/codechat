package org.kobjects.codechat.type;

public abstract class CollectionType extends TupleType {

    public final Type elementType;

    public CollectionType(String name, Type elementType) {
        super(name + "[" + elementType + "]");
        this.elementType = elementType;
        addProperty(0, "size", Type.NUMBER, false, "The number of contained elements.");
    }

    public boolean isAssignableFrom(Type other) {
        if (!(other instanceof CollectionType)) {
            return false;
        }
        CollectionType otherType = (CollectionType) other;
        return otherType.getClass() == getClass() && elementType.isAssignableFrom(otherType.elementType);
    }

}
