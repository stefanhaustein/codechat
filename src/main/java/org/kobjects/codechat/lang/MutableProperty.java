package org.kobjects.codechat.lang;

import org.kobjects.codechat.lang.Property;

public class MutableProperty<T> extends Property<T> {
    public MutableProperty(T value) {
        super(value);
    }
}
