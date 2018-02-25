package org.kobjects.codechat.instance;

/**
 * Property backed by a stored value
 */
public class MaterialProperty<T> extends Property<T> {
    protected T value;
    private final T initialValue;

    public MaterialProperty(T value) {
        this.initialValue = this.value = value;
    }

    public void set(T newValue) {
        T oldValue;
        synchronized(this) {
            if (newValue == this.value || this.value != null && this.value.equals(newValue)) {
                return;
            }
            oldValue = this.value;
            this.value = newValue;
        }
        notifyChanged(newValue, oldValue);
    }

    public T get() {
        return value;
    }


    public boolean modified() {
        return value != initialValue;
    }

}
