package org.kobjects.codechat.lang;

public class MutableProperty<T> extends MaterialProperty<T> {
    private final T initialValue;

    public MutableProperty(T value) {
        super(value);
        this.initialValue = value;
    }


    public boolean modified() {
        return value != initialValue;
    }

}
