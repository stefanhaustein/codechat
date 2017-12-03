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

public class Text extends AbstractViewWrapper<TextView> implements Runnable {
    public final static InstanceType TYPE = new ViewWrapperType<Text>() {
        @Override
        public Text createInstance(Environment environment, int id) {
            return new Text((AndroidEnvironment) environment, id);
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
        TYPE    .addProperty(4, "size", Type.NUMBER, true,
                    "The font size in normalized pixels.")
                .addProperty(5, "text", Type.STRING, true, "The displayed text string.");
        }


    private double width;
    private double height;
    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<String> text = new VisualMaterialProperty<>("");


    public Text(AndroidEnvironment environment, int id) {
        super(environment, id, new TextView(environment.rootView.getContext()));
        this.text.set("Text#" + id);
        syncView();
    }

    public void run() {
        syncRequested = false;
        if (detached) {
            if (view.getParent() != null) {
                environment.rootView.removeView(view);
            }
            return;
        }
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
        height = bounds.height() / environment.scale;
        width = bounds.width() / environment.scale;

        view.setX((float) (environment.scale * getNormalizedX()));
        view.setY((float) (environment.scale * getNormalizedY()));

        if (view.getParent() == null) {
            this.environment.rootView.addView(view);
        }

        /*
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = Math.round((float) (environment.scale * size));
        if (params.height != params.width) {
            params.height = params.width;
            view.requestLayout();
        }*/
    }

    @Override
    public InstanceType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 4: return size;
            case 5: return text;
            default:
                return super.getProperty(index);
        }
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }


}
