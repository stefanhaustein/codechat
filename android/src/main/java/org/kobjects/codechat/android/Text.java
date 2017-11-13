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

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<String> text = new VisualMaterialProperty<>("");


    public Text(AndroidEnvironment environment, int id) {
        super(environment, id, new TextView(environment.rootView.getContext()));
        this.text.set("Text#" + id);
        syncView();
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

        switch (xAlign.get()) {
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

        switch (yAlign.get()) {
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
            case 4: return size;
            case 5: return text;
            default:
                return super.getProperty(index);
        }
    }


}
