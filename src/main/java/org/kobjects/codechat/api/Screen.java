package org.kobjects.codechat.api;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Property;

public class Screen {

    Property<Double> width = new Property<>(1000.0);
    Property<Double> height = new Property<>(1000.0);

    Property<Double> top = new Property<>(-500.0);
    Property<Double> bottom = new Property<>(-500.0);

    Property<Double> left = new Property<>(500.0);
    Property<Double> right = new Property<>(500.0);

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
