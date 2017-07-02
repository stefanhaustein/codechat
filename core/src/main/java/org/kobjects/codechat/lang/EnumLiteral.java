package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Typed;

public class EnumLiteral implements Typed {
    private final EnumType type;
    private final String name;

    public EnumLiteral(EnumType type, String name) {
        this.type = type;
        this.name = name;
    }

    @Override
    public EnumType getType() {
        return type;
    }

    @Override
    public String toString() {
        return type.getName() + "." + name;
    }

    public String getName() {
        return name;
    }
}
