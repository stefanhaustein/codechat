package org.kobjects.codechat.android;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.view.ViewParent;

import android.widget.TextView;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class Text extends Instance implements Runnable {
    public final static InstanceType TYPE = new InstanceType() {

        @Override
        public Text createInstance(Environment environment, int id) {
            return new Text(environment, id);
        }

        @Override
        public String getName() {
            return "Text";
        }

        @Override
        public AnnotatedCharSequence getDocumentation() {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            asb.append("A text object displayed on the screen.");
            asb.append(super.getDocumentation());
            return asb.build();
        }
    };
    static {
        TYPE.addProperty(0, "size", Type.NUMBER, true,
                "The font size in normalized pixels.");
        TYPE.addProperty(1, "x", Type.NUMBER, true,
                "The horizontal position of the text in normalized pixels, relative to the left side, " +
                        "center or right side of the screen, depending on the value of the horizontalAlignment property.");
        TYPE.addProperty(2, "y", Type.NUMBER, true,
                "The vertical position of the text in normalized pixels relative to the top, " +
                        "center or bottom of the screen, depending on the value of the yAlign property. ");
        TYPE.addProperty(3, "horizontalAlignment", AndroidEnvironment.XAlign.TYPE, true,
                "Determines whether the x property is relative to the left side, " +
                        "center or right side of the screen.");
        TYPE.addProperty(4, "yAlign", AndroidEnvironment.YAlign.TYPE, true,
                "Determines whether the x property is relative to the left side, " +
                        "center or right side of the screen.");
        TYPE.addProperty(5, "text", Type.STRING, true, "The displayed text string.");
    }

    private final TextView view;
    private boolean syncRequested;
    AndroidEnvironment environment;

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<Double> x = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> y = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<String> text = new VisualMaterialProperty<>("");
    public VisualMaterialProperty<AndroidEnvironment.XAlign> horizonalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.XAlign.CENTER);
    public VisualMaterialProperty<AndroidEnvironment.YAlign> verticalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.YAlign.CENTER);


    public Text(Environment environment, int id) {
        super(environment, id);
        this.environment = (AndroidEnvironment) environment;
        this.text.set("Text#" + id);
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
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size / environment.scale));
        if (!text.get().equals(view.getText())) {
            view.setText(text.get());
        }
        /*
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        double width = view.getMeasuredWidth() / environment.scale;
        double height = view.getMeasuredHeight() / environment.scale;
        */

        Rect bounds = new Rect();
        Paint textPaint = view.getPaint();
        textPaint.getTextBounds(text.get(),0,text.get().length(),bounds);
        double height = bounds.height() / environment.scale;
        double width = bounds.width() / environment.scale;

        switch (horizonalAlignment.get()) {
            case LEFT:
                view.setX((float) (environment.scale * (x.get())));
                break;
            case CENTER:
                view.setX((float) (environment.rootView.getMeasuredWidth()/2 + environment.scale * (x.get() - width / 2)));
                break;
            case RIGHT:
                view.setX((float) (environment.rootView.getMeasuredWidth() - environment.scale * (x.get() + width)));
                break;
        }

        switch (verticalAlignment.get()) {
            case TOP:
                view.setY((float) (environment.scale * (y.get())));
                break;
            case CENTER:
                view.setY(environment.rootView.getMeasuredHeight() / 2 - (float) (environment.scale * (y.get() + height / 2)));
                break;
            case BOTTOM:
                view.setY(environment.rootView.getMeasuredHeight() - (float) (environment.scale * (y.get() + height)));
                break;
        }

        /*
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = Math.round((float) (environment.scale * size));
        if (params.height != params.width) {
            params.height = params.width;
            view.requestLayout();
        }*/
    }

    public void delete() {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
    }

    @Override
    public InstanceType getType() {
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
