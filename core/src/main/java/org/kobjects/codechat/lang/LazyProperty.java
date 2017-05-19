package org.kobjects.codechat.lang;

import java.util.Objects;

public abstract class LazyProperty<T> extends Property<T> {

    boolean valid;
    T value;

    private void validate() {
        if (!valid) {
            valid = true;
            T oldValue = value;
            value = compute();
            if (!value.equals(oldValue)) {
                notifyChanged(oldValue, value);
            }
        }
    }

    public void invalidate() {
        valid = false;
        if (hasListeners()) {
            validate();
        }
    }

    protected abstract T compute();

    @Override
    public T get() {
        validate();
        return value;
    }
}
