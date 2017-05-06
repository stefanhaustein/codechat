package org.kobjects.codechat.api;

import org.kobjects.codechat.lang.MaterialProperty;

public class Screen {

    MaterialProperty<Double> width = new MaterialProperty<>(1000.0);
    MaterialProperty<Double> height = new MaterialProperty<>(1000.0);

    MaterialProperty<Double> top = new MaterialProperty<>(-500.0);
    MaterialProperty<Double> bottom = new MaterialProperty<>(-500.0);

    MaterialProperty<Double> left = new MaterialProperty<>(500.0);
    MaterialProperty<Double> right = new MaterialProperty<>(500.0);

    public MaterialProperty<Double> frame = new MaterialProperty<>(0.0);

    int oldNativeHeight;
    int oldNativeWidth;

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
