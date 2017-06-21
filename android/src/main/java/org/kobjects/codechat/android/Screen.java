package org.kobjects.codechat.android;

import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Tuple;
import org.kobjects.codechat.type.TupleType;

public class Screen implements Tuple {

    public static final TupleType TYPE = new TupleType("screenType");
    static {
        TYPE.addProperty(0, "width", TYPE.NUMBER, false);
        TYPE.addProperty(1, "height", TYPE.NUMBER, false);
        TYPE.addProperty(2, "top", TYPE.NUMBER, false);
        TYPE.addProperty(3, "bottom", TYPE.NUMBER, false);
        TYPE.addProperty(4, "left", TYPE.NUMBER, false);
        TYPE.addProperty(5, "right", TYPE.NUMBER, false);
        TYPE.addProperty(6, "frame", TYPE.NUMBER, false);
    }

    MaterialProperty<Double> width = new MaterialProperty<>(1000.0);
    MaterialProperty<Double> height = new MaterialProperty<>(1000.0);

    MaterialProperty<Double> top = new MaterialProperty<>(-500.0);
    MaterialProperty<Double> bottom = new MaterialProperty<>(-500.0);

    MaterialProperty<Double> left = new MaterialProperty<>(500.0);
    MaterialProperty<Double> right = new MaterialProperty<>(500.0);

    public MaterialProperty<Double> frame = new MaterialProperty<>(0.0);

    int oldNativeHeight;
    int oldNativeWidth;

    @Override
    public TupleType getType() {
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
                scaledHeight = 1000;
                scaledWidth = Math.round((500.0 * nativeWidth) / nativeHeight) * 2;
            } else {
                scaledWidth = 1000;
                scaledHeight = Math.round((500.0 * nativeHeight) / nativeWidth) * 2;
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

}
