package org.kobjects.codechat.lang;

import java.util.ArrayList;

public abstract class Property<T> {
    protected ArrayList<MaterialProperty.PropertyListener<T>> listeners;

    public abstract T get();

    public void addListener(MaterialProperty.PropertyListener<T> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public void removeListener(MaterialProperty.PropertyListener<T> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    boolean hasListeners() {
        return listeners != null && listeners.size() > 0;
    }

    void notifyChanged(T oldValue, T newValue) {
        if (listeners != null) {
            for (MaterialProperty.PropertyListener<T> listener : listeners) {
                listener.valueChanged(this, oldValue, newValue);
            }
        }
    }

    public interface PropertyListener<T> {
        void valueChanged(Property<T> property, T oldValue, T newValue);
    }

}
