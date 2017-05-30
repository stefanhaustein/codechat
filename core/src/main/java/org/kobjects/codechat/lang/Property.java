package org.kobjects.codechat.lang;

import java.util.ArrayList;

public abstract class Property<T> {
    protected ArrayList<PropertyListener<T>> listeners;

    public abstract T get();

    public void addListener(PropertyListener<T> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(PropertyListener<T> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public boolean hasListeners() {
        return listeners != null && listeners.size() > 0;
    }

    public void notifyChanged(T oldValue, T newValue) {
        if (listeners != null) {
            for (PropertyListener<T> listener : listeners) {
                listener.valueChanged(this, oldValue, newValue);
            }
        }
    }

    public void removeAllListeners() {
        listeners = null;
    }

    public interface PropertyListener<T> {
        void valueChanged(Property<T> property, T oldValue, T newValue);
    }

}
