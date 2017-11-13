package org.kobjects.codechat.android;

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

    public VisualMaterialProperty<Double> x = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> y = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<AndroidEnvironment.XAlign> xAlign = new VisualMaterialProperty<>(AndroidEnvironment.XAlign.CENTER);
    public VisualMaterialProperty<AndroidEnvironment.YAlign> yAlign = new VisualMaterialProperty<>(AndroidEnvironment.YAlign.CENTER);


    protected AbstractViewWrapper(AndroidEnvironment environment, int id, T view) {
        super(environment, id);
        this.environment = environment;
        this.view = view;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return x;
            case 1: return y;
            case 2: return xAlign;
            case 3: return yAlign;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void syncView() {
        if (!syncRequested) {
            syncRequested = true;
            view.post(this);
        }
    }

    public static abstract class ViewWrapperType<T extends AbstractViewWrapper> extends InstanceType<T> {

        protected ViewWrapperType() {
            addProperty(0, "x", Type.NUMBER, true,
                    "The horizontal position of the sprite, relative to the left side, " +
                            "center or right side of the screen, depending on the value of the xAlign property.");
            addProperty(1, "y", Type.NUMBER, true,
                    "The vertical position of the sprite relative to the top, " +
                            "center or bottom of the screen, depending on the value of the yAlign property. ");
            addProperty(2, "xAlign", AndroidEnvironment.XAlign.TYPE, true,
                    "Determines whether the x property is relative to the left side, " +
                            "center or right side of the screen.");
            addProperty(3, "yAlign", AndroidEnvironment.YAlign.TYPE, true,
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
