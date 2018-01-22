package org.kobjects.codechat.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
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
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class Text extends AbstractViewWrapper<AppCompatTextView> implements Runnable {

    public final static InstanceType TYPE = new ViewWrapperType<Text>() {
        @Override
        public Text createInstance(Environment environment) {
            return new Text((AndroidEnvironment) environment);
        }

        @Override
        public String toString() {
            return "Text";
        }

        @Override
        public void printDocumentationBody(AnnotatedStringBuilder asb) {
            asb.append("Text\n\n", new Title());
            asb.append("A text object displayed on the screen.");
        }
    };

    static int count;
    static {
        TYPE.addProperty(6, "size", Type.NUMBER, true,
                    "The font size in normalized pixels.");
        TYPE.addProperty(7, "text", Type.STRING, true, "The displayed text string.");
        TYPE.addProperty(8, "color", Type.NUMBER, true, "The text color.");
    }

    private double width;
    private double height;
    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<String> text = new VisualMaterialProperty<>("");
    public VisualMaterialProperty<Double> color = new VisualMaterialProperty<>(0.0);

    public Text(AndroidEnvironment environment) {
        super(environment, new AppCompatTextView(environment.rootView.getContext()));
        this.text.set("Text");
        syncView();
    }

    public void run() {
        syncRequested = false;
        double size = this.size.get();
    //    view.setBackgroundColor(0x88ff8888);

        view.setTextColor(((Math.round(opacity.get().floatValue() * 255)) << 24)
                        | (((int) color.get().doubleValue()) & 0x0ffffff));
        view.setZ(z.get().floatValue());

        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, (float) (size * environment.scale));
        if (!text.get().equals(view.getText())) {
            view.setText(text.get());
        }

        int spec = View.MeasureSpec.makeMeasureSpec(count++ & 0xffff, View.MeasureSpec.UNSPECIFIED);
        view.measure(spec, spec);

        /*
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        double width = view.getMeasuredWidth() / environment.scale;
        double height = view.getMeasuredHeight() / environment.scale;
        */

//        view.measureActually();

        height = view.getMeasuredHeight() / environment.scale;
        width =  view.getMeasuredWidth() / environment.scale;

        System.out.println("************************** new w/h: " + width + ", " + height);

        float screenX = (float) (environment.scale * getNormalizedX(environment.screen.width.get()));
        float screenY = (float) (environment.scale * getNormalizedY(environment.screen.height.get()));

        view.setX(screenX);
        view.setY(screenY);

       // System.out.println("********************************* View: " + view + " text: "+ view.getText());

        if (view.getParent() == null) {
            this.environment.rootView.addView(view);
        }
//        view.getLayoutParams().height = view.getMeasuredHeight();
        //      view.getLayoutParams().width = view.getMeasuredWidth();
    }

    @Override
    public InstanceType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 6: return size;
            case 7: return text;
            case 8: return color;
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

    /*

    static int n = 0;

    static class MyTextView extends android.support.v7.widget.AppCompatTextView {

        public MyTextView(Context context) {
            super(context);
        }

        @SuppressLint("WrongCall")
        public void measureActually() {
            int spec =
        }
    }
*/
}
