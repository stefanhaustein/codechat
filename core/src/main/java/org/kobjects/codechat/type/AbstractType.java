package org.kobjects.codechat.type;

public abstract class AbstractType implements Type {



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
