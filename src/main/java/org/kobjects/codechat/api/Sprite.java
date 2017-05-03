package org.kobjects.codechat.api;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.MutableProperty;
import org.kobjects.codechat.lang.Property;

public class Sprite extends Instance implements Ticking, Runnable {

    private final ImageView view;
    private boolean syncRequested;

    public VisualProperty<Double> size = new VisualProperty<>(100.0);
    public VisualProperty<Double> x = new VisualProperty<>(0.0);
    public VisualProperty<Double> y = new VisualProperty<>(0.0);
    public VisualProperty<Double> rotation = new VisualProperty<>(0.0);
    public VisualProperty<Emoji> face = new VisualProperty<>(new Emoji(new String(Character.toChars(0x1f603))));

    public MutableProperty<Double> dx = new MutableProperty<>(0.0);
    public MutableProperty<Double> dy = new MutableProperty<>(0.0);
    public MutableProperty<Double> rotationSpeed = new MutableProperty<>(0.0);
    public Property<Boolean> touched = new MutableProperty<>(false);

    private Emoji lastFace;

    public Sprite(Environment environment, int id) {
        super(environment, id);
        view = new ImageView(environment.rootView.getContext());
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touched.set(true);
                    return true;
                }
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    touched.set(false);
                    return true;
                }
                return false;
            }
        });
        environment.rootView.addView(view);
        syncView();
    }

    public void move(double x, double y) {
        this.x.set(x);
        this.y.set(y);
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
        view.setX((float) (environment.rootView.getMeasuredWidth()/2 + environment.scale * (x.get() - size / 2)));
        view.setY(environment.rootView.getMeasuredHeight()/2 - (float) (environment.scale * (y.get() + size / 2)));
        view.setRotation(rotation.get().floatValue());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = params.height = Math.round((float) (environment.scale * size));
        view.requestLayout();
        if (face.get() != lastFace) {
            lastFace = face.get();
            view.setImageDrawable(lastFace.getDrawable(view.getContext()));
        }
    }

/*
    public Emoji getFace() {
        return new Emoji(view.getText().toString());
    }
*/
    @Override
    public void tick(boolean force) {
        double dxValue = dx.get();
        double dyValue = dy.get();
        if (force || dxValue != 0 || dyValue != 0) {
            Screen screen = environment.screen;
            double xValue = x.get();
            double yValue = y.get();
            double sizeValue = size.get();

            if (dxValue > 0 && xValue > screen.right.get() + sizeValue) {
                x.set(screen.left.get() - sizeValue);
            } else if (dxValue < 0 && xValue < screen.left.get() - sizeValue) {
                x.set(screen.right.get() + sizeValue);
            } else {
                x.set(xValue + dxValue);
            }

            if (dyValue > 0 && yValue > screen.top.get() + sizeValue) {
                y.set(screen.bottom.get() - sizeValue);
            } else if (dyValue < 0 && yValue < screen.bottom.get() - sizeValue) {
                y.set(screen.top.get() + sizeValue);
            } else {
                y.set(yValue + dyValue);
            }
        }
        double rotationSpeedVaue = rotationSpeed.get();
        if (rotationSpeedVaue != 0) {
            rotation.set(rotation.get() + rotationSpeedVaue);
        }
    }

    public void delete() {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
        environment.ticking.remove(this);
    }


    class VisualProperty<T> extends MutableProperty<T> {

        public VisualProperty(T value) {
            super(value);
        }


        @Override
        public void set(T value) {
            super.set(value);
            syncView();
        }
    }

}
