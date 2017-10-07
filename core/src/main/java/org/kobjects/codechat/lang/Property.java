package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Collections;

public abstract class Property<T> {
    protected ArrayList<PropertyListener<T>> listeners;

    public abstract T get();

    public synchronized void addListener(PropertyListener<T> listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);
    }

    public synchronized void removeListener(PropertyListener<T> listener) {
        if (listeners != null) {
            listeners.remove(listener);
        }
    }

    public synchronized boolean hasListeners() {
        return listeners != null && listeners.size() > 0;
    }

    public synchronized void notifyChanged(final T oldValue, final T newValue) {
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

    public synchronized void removeAllListeners() {
        listeners = null;
    }

    public synchronized Iterable<PropertyListener<T>> getListeners() {
        if (listeners == null) {
            return Collections.emptyList();
        }
        return listeners;
    }

    public interface PropertyListener<T> {
        void valueChanged(Property<T> property, T oldValue, T newValue);
    }

    public void set(T value) {
        throw new UnsupportedOperationException();
    }
}
