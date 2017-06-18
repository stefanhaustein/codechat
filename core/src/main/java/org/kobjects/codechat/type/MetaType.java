package org.kobjects.codechat.type;


public class MetaType extends Type {
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
    public String getName() {
        return "metatype[" + type + "]";
    }

    @Override
    public Class<?> getJavaClass() {
        return Type.class;
    }

    public Type getType() {
        return type;
    }
}
