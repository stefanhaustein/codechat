package org.kobjects.codechat.android;

import android.app.Activity;
import android.view.View;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public abstract class AbstractViewWrapper<T extends View> extends Instance implements Runnable {

    protected final T view;
    protected final AndroidEnvironment environment;
    protected boolean syncRequested;

    // True if this sprite was deleted.
    protected boolean detached;

    public VisualMaterialProperty<Double> x = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> y = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> z = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<AndroidEnvironment.XAlign> xAlign = new VisualMaterialProperty<>(AndroidEnvironment.XAlign.CENTER);
    public VisualMaterialProperty<AndroidEnvironment.YAlign> yAlign = new VisualMaterialProperty<>(AndroidEnvironment.YAlign.CENTER);


    protected AbstractViewWrapper(AndroidEnvironment environment, int id, T view) {
        super(environment, id);
        this.environment = environment;
        this.view = view;
        view.setTag(this);
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return x;
            case 1: return y;
            case 2: return z;
            case 3: return xAlign;
            case 4: return yAlign;
            default:
                throw new IllegalArgumentException();
        }
    }

    public abstract double getWidth();

    public abstract double getHeight();

    public double getNormalizedX() {
        switch (xAlign.get()) {
            case LEFT:
                return x.get();
            case RIGHT:
                return environment.screen.width.get() - x.get() - getWidth();
            default:
                return environment.screen.width.get() / 2 + x.get() - getWidth()/2;
        }
    }

    public double getNormalizedY() {
        switch (yAlign.get()) {
            case TOP:
                return y.get();
            case BOTTOM:
                return environment.screen.height.get() - y.get() - getHeight();
            default:
                return environment.screen.height.get() / 2 - y.get() - getHeight() / 2;
        }
    }

    public void syncView() {
        if (!syncRequested) {
            syncRequested = true;
            ((Activity) environment.context).runOnUiThread(this);
        }
    }

    public void delete() {
        if (detached) {
            return;
        }
        detached = true;
        syncView();
    }

    public static abstract class ViewWrapperType<T extends AbstractViewWrapper> extends InstanceType<T> {

        protected ViewWrapperType() {
            addProperty(0, "x", Type.NUMBER, true,
                    "The horizontal position of the object, relative to the left side, " +
                            "center or right side of the screen, depending on the value of the xAlign property.");
            addProperty(1, "y", Type.NUMBER, true,
                    "The vertical position of the object, relative to the top, " +
                            "center or bottom of the screen, depending on the value of the yAlign property. ");
            addProperty(2, "z", Type.NUMBER, true,
                    "The z position of the object; objects with a higher value will be displayed on top.");
            addProperty(3, "xAlign", AndroidEnvironment.XAlign.TYPE, true,
                    "Determines whether the x property is relative to the left side, " +
                            "center or right side of the screen.");
            addProperty(4, "yAlign", AndroidEnvironment.YAlign.TYPE, true,
                    "Determines whether the y property is relative to the top, " +
                            "center or bottom of the screen.");
        }

    }

    class VisualMaterialProperty<T> extends MaterialProperty<T> {

        public VisualMaterialProperty(T value) {
            super(value);
        }


        @Override
        public void set(T newValue) {
            super.set(newValue);
            syncView();
        }
    }

}
