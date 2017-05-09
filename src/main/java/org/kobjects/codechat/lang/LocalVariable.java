package org.kobjects.codechat.lang;

public class LocalVariable {
    private final String name;
    private final int index;
    private final Type type;

    LocalVariable(String name, Type type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
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
