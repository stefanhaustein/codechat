package org.kobjects.codechat.lang;

import org.kobjects.codechat.lang.Property;

public class MutableProperty<T> extends Property<T> {
    private final T initialValue;

    public MutableProperty(T value) {
        super(value);
        this.initialValue = value;
    }


    public boolean modified() {
        return value != initialValue;
    }

}
