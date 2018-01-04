package org.kobjects.codechat.type;


public class MetaType extends AbstractType {
    Type type;
    public MetaType(Type type) {
        this.type = type;
    }

    @Override
    public boolean isAssignableFrom(Type other) {
        if (!(other instanceof MetaType)) {
            return false;
        }
        return type.equals(((MetaType) other).type);
    }

    @Override
    public String toString() {
        return "metatype[" + type + "]";
    }

    public Type getType() {
        return type;
    }
}
