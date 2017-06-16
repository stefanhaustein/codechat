package org.kobjects.codechat.type;


public abstract class Type implements Typed {
    public static final Type NUMBER = new SimpleType("number", Double.class);
    public static final Type STRING = new SimpleType("string", String.class);
    public static final Type BOOLEAN = new SimpleType("boolean", Boolean.class);
    public static final Type VOID = new SimpleType("void", Void.TYPE);
    public static final Type ANY = new SimpleType("any", Object.class);

    public static Class<?> getJavaClassForSignature(Type type) {
        Class javaClass = type.getJavaClass();
        return javaClass == Double.class ? Double.TYPE : javaClass == Boolean.class ? Boolean.TYPE : javaClass;
    }

    public static Type of(Object o) {
        // Fixme: Add "typed" interface.
        if (o == null) {
            return VOID;
        }
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        if (o instanceof Typed) {
            return ((Typed) o).getType();
        }
        if (o instanceof Boolean) {
            return Type.BOOLEAN;
        }
        if (o instanceof Double) {
            return Type.NUMBER;
        }
        if (o instanceof String) {
            return Type.STRING;
        }
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        return Type.ANY;
    }


    // FIXME: Abstract
    public boolean isAssignableFrom(Type other) {
        return getJavaClass().isAssignableFrom(other.getJavaClass());
    }

    public abstract String getName();

    // FIXME: Remove
    public abstract Class<?> getJavaClass();

    @Override
    public final String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Type)) {
            return false;
        }
        Type t2 = (Type) other;
        return isAssignableFrom(t2) && t2.isAssignableFrom(this);
    }

    @Override
    public Type getType() {
        return new MetaType(this);
    }
}
