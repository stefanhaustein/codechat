package org.kobjects.codechat.android;

import android.view.ViewGroup;
import android.view.ViewParent;

import android.widget.TextView;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class Text extends TupleInstance implements Runnable {
    public final static TupleType TYPE = new TupleType("Text") {
        @Override
        public Text createInstance(Environment environment, int id) {
            return new Text(environment, id);
        }
    };
    static {
        TYPE.addProperty(0, "size", Type.NUMBER, true);
        TYPE.addProperty(1, "x", Type.NUMBER, true);
        TYPE.addProperty(2, "y", Type.NUMBER, true);
        TYPE.addProperty(3, "horizontalAlignment", AndroidEnvironment.HorizontalAlignment, true);
        TYPE.addProperty(4, "verticalAlignment", AndroidEnvironment.VerticalAlignment, true);
        TYPE.addProperty(5, "text", Type.STRING, true);
    }

    private final TextView view;
    private boolean syncRequested;
    AndroidEnvironment environment;

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(100.0);
    public VisualMaterialProperty<Double> x = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> y = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<String> text = new VisualMaterialProperty<>("");
    public VisualMaterialProperty<EnumLiteral> horizonalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.HorizontalAlignment.getValue("CENTER"));
    public VisualMaterialProperty<EnumLiteral> verticalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.VerticalAlignment.getValue("CENTER"));


    public Text(Environment environment, int id) {
        super(environment, id);
        this.environment = (AndroidEnvironment) environment;
        view = new TextView(this.environment.rootView.getContext());
        this.environment.rootView.addView(view);
        syncView();
    }


    public void syncView() {
        if (!syncRequested) {
            syncRequested = true;
            view.post(this);
        }
    }

    public void run() {
        syncRequested = false;
        double size = this.size.get();
        switch (horizonalAlignment.get().getName()) {
            case "LEFT":
                view.setX((float) (environment.scale * (x.get())));
                break;
            case "CENTER":
                view.setX((float) (environment.rootView.getMeasuredWidth()/2 + environment.scale * (x.get() - size / 2)));
                break;
            case "RIGHT":
                view.setX((float) (environment.rootView.getMeasuredWidth() - environment.scale * (x.get() + size)));
                break;
        }

        switch (verticalAlignment.get().getName()) {
            case "TOP":
                view.setY((float) (environment.scale * (y.get())));
                break;
            case "CENTER":
                view.setY(environment.rootView.getMeasuredHeight() / 2 - (float) (environment.scale * (y.get() + size / 2)));
                break;
            case "BOTTOM":
                view.setY(environment.rootView.getMeasuredHeight() - (float) (environment.scale * (y.get() + size)));
                break;
        }

        /*
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = Math.round((float) (environment.scale * size));
        if (params.height != params.width) {
            params.height = params.width;
            view.requestLayout();
        }*/
        if (!text.get().equals(view.getText())) {
            view.setText(text.get());
        }
    }

    public void delete() {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    @Override
    public TupleType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return size;
            case 1: return x;
            case 2: return y;
            case 3: return horizonalAlignment;
            case 4: return verticalAlignment;
            case 5: return text;
            default:
                throw new IllegalArgumentException();
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
