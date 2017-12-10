package org.kobjects.codechat.android;

import android.graphics.Paint;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import android.widget.TextView;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
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
        TYPE    .addProperty(5, "size", Type.NUMBER, true,
                    "The font size in normalized pixels.")
                .addProperty(6, "text", Type.STRING, true, "The displayed text string.");
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
    //    view.setBackgroundColor(0x88ff8888);
        view.setTextColor(0x0ff000000);
        view.setZ(z.get().floatValue());

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size * environment.scale));
        if (!text.get().equals(view.getText())) {
            view.setText(text.get());
        }
        /*
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        double width = view.getMeasuredWidth() / environment.scale;
        double height = view.getMeasuredHeight() / environment.scale;
        */


        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(widthMeasureSpec, heightMeasureSpec);

        height = view.getMeasuredHeight() / environment.scale;
        width =  view.getMeasuredWidth() / environment.scale;

        float screenX = (float) (environment.scale * getNormalizedX());
        float screenY = (float) (environment.scale * getNormalizedY());

        view.setX(screenX);
        view.setY(screenY);

       // System.out.println("********************************* View: " + view + " text: "+ view.getText());

        if (view.getParent() == null) {
            this.environment.rootView.addView(view);
        }
        view.getLayoutParams().height = view.getMeasuredHeight();
        view.getLayoutParams().width = view.getMeasuredWidth();
    }

    @Override
    public InstanceType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 5: return size;
            case 6: return text;
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
