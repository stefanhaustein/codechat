package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.Type;

public class LocalVariable {
    private final String name;
    private final int index;
    private final Type type;
    private final boolean constant;

    LocalVariable(String name, Type type, int index, boolean constant) {
        this.name = name;
        this.type = type;
        this.index = index;
        this.constant = constant;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public int getIndex() {
        return index;
    }
}
