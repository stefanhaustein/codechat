package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Objects;

public class Property<T> {
    protected T value;
    ArrayList<PropertyListener<T>> listeners;

    public Property(T value) {
        this.value = value;
    }

    public void set(T value) {
        if (value == this.value || this.value != null && this.value.equals(value)) {
            return;
        }
        T oldValue = value;
        this.value = value;
        if (listeners != null) {
            for (PropertyListener<T> listener : listeners) {
                listener.valueChanged(this, oldValue, value);
            }
        }
    }

    public T get() {
        return value;
    }

    public void addListener(PropertyListener<T> listener) {
        listeners.add(listener);
    }

    public void removeListener(PropertyListener<T> listener) {
        listeners.remove(listener);
    }


    public interface PropertyListener<T> {
        void valueChanged(Property<T> property, T oldValue, T newValue);
    }

}
