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

    public void notifyChanged(final T oldValue, final T newValue) {
        if (listeners != null) {
            for (final PropertyListener<T> listener : listeners) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listener.valueChanged(Property.this, oldValue, newValue);
                    }
                }).start();

            }
        }
    }

    public void removeAllListeners() {
        listeners = null;
    }

    public interface PropertyListener<T> {
        void valueChanged(Property<T> property, T oldValue, T newValue);
    }

    public void set(T value) {
        throw new UnsupportedOperationException();
    }
}
