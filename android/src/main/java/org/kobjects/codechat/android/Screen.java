package org.kobjects.codechat.android;

import java.lang.ref.WeakReference;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.type.InstanceType;

public class Screen extends Instance {

    public static final InstanceType<Screen> TYPE = new InstanceType<Screen>(true) {
        @Override
        public String getName() {
            return "Screen";
        }
        @Override
        public AnnotatedCharSequence getDocumentation() {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            asb.append("The screen object contains information about the visible device screen such as the dimensions.");
            asb.append(super.getDocumentation());
            return asb.build();
        }
    };
    static {
        TYPE.addProperty(0, "width", TYPE.NUMBER, false,
                "The width of the usable area of the screen in normalized pixels. "+
                        "Pixels are normalized so that the smaller value of width and the height is at least 100.");
        TYPE.addProperty(1, "height", TYPE.NUMBER, false,
                "The height of the usable area of the screen in normalized pixels. "+
                        "Pixels are normalized so that the smaller value of width and the height is at least 100.");
        TYPE.addProperty(2, "top", TYPE.NUMBER, false,
                "The top boundary of the usable screen are, counted from the center. At least 50.");
        TYPE.addProperty(3, "bottom", TYPE.NUMBER, false,
                "The bottom boundary of the usable screen area, counted from the center. " +
                        "This value is always negative.");
        TYPE.addProperty(4, "left", TYPE.NUMBER, false,
                "The left boundary of the usable screen area, counted from the center. " +
                "This value is always negative.");
        TYPE.addProperty(5, "right", TYPE.NUMBER, false,
                "The right boundary of the usable screen are, counted from the center. At least 50.");
        TYPE.addProperty(6, "frame", TYPE.NUMBER, false,
                "A counter that is incremented every time the screen content is updated.");
    }

    MaterialProperty<Double> width = new MaterialProperty<>(100.0);
    MaterialProperty<Double> height = new MaterialProperty<>(100.0);

    MaterialProperty<Double> top = new MaterialProperty<>(-50.0);
    MaterialProperty<Double> bottom = new MaterialProperty<>(-50.0);

    MaterialProperty<Double> left = new MaterialProperty<>(50.0);
    MaterialProperty<Double> right = new MaterialProperty<>(50.0);

    public MaterialProperty<Double> frame = new MaterialProperty<>(0.0);

    int oldNativeHeight;
    int oldNativeWidth;

    protected Screen(Environment environment) {
        super(environment, NO_ID);
    }

    @Override
    public InstanceType<Screen> getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return width;
            case 1: return height;
            case 2: return top;
            case 3: return bottom;
            case 4: return left;
            case 5: return right;
            case 6: return frame;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void update(int nativeWidth, int nativeHeight) {
        if (nativeWidth != oldNativeWidth || nativeHeight != oldNativeHeight) {

            double scaledWidth;
            double scaledHeight;

            if (nativeWidth > nativeHeight) {
                scaledHeight = 100;
                scaledWidth = Math.round((50.0 * nativeWidth) / nativeHeight) * 2;
            } else {
                scaledWidth = 100;
                scaledHeight = Math.round((50.0 * nativeHeight) / nativeWidth) * 2;
            }

            left.set(-scaledWidth / 2);
            bottom.set(-scaledHeight / 2);
            right.set(scaledWidth / 2);
            top.set(scaledHeight / 2);
            width.set(scaledWidth);
            height.set(scaledHeight);

            oldNativeHeight = nativeHeight;
            oldNativeWidth = nativeWidth;
        }
    }

    public String toString() {
        return "screen";
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        super.getDependencies(result);
        for (WeakReference<Sprite> spriteRef : Sprite.allSprites) {
            Sprite sprite = spriteRef.get();
            if (sprite != null && sprite.view.getParent() != null) {
                result.add(sprite);
            }
        }
    }

    @Override
    public void delete() {

    }
}
