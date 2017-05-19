package org.kobjects.codechat.android;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import org.kobjects.codechat.lang.*;

public class Sprite extends Instance implements Ticking, Runnable {

    private final ImageView view;
    private boolean syncRequested;
    private static List<Sprite> allVisibleSprites = new ArrayList();
    AndroidEnvironment environment;

    public VisualProperty<Double> size = new VisualProperty<>(100.0);
    public VisualProperty<Double> x = new VisualProperty<>(0.0);
    public VisualProperty<Double> y = new VisualProperty<>(0.0);
    public VisualProperty<Double> rotation = new VisualProperty<>(0.0);
    public VisualProperty<String> face = new VisualProperty<>(new String(Character.toChars(0x1f603)));
    public LazyProperty<List<Sprite>> collisions = new LazyProperty<List<Sprite>>() {
        @Override
        protected List<Sprite> compute() {
            ArrayList<Sprite> result = new ArrayList<>();
            double x = Sprite.this.x.get();
            double y = Sprite.this.y.get();
            double size = Sprite.this.size.get();
            synchronized (allVisibleSprites) {
                for (Sprite other : allVisibleSprites) {
                    if (other != Sprite.this) {
                        double distX = other.x.get() - x;
                        double distY = other.y.get() - y;
                        double minDist = (other.size.get() + size) / 2;
                        if (distX * distX + distY * distY < minDist * minDist) {
                            result.add(other);
                        }
                    }
                }
            }
            return result;
        }
    };

    public MutableProperty<Double> dx = new MutableProperty<>(0.0);
    public MutableProperty<Double> dy = new MutableProperty<>(0.0);
    public MutableProperty<Double> rotationSpeed = new MutableProperty<>(0.0);
    public MaterialProperty<Boolean> touched = new MutableProperty<>(false);

    public LazyProperty<Boolean> visible = new LazyProperty<Boolean>() {
        @Override
        protected Boolean compute() {
            Screen screen = environment.screen;
            double size2 = size.get() / 2;
            return x.get() >= screen.left.get() - size2 &&
                    x.get() <= screen.right.get() + size2 &&
                    y.get() >= screen.bottom.get() - size2 &&
                    y.get() <= screen.top.get() + size2;
        }
    };

    private String lastFace;

    public Sprite(Environment environment, int id) {
        super(environment, id);
        this.environment = (AndroidEnvironment) environment;
        view = new ImageView(this.environment.rootView.getContext());
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
        this.environment.rootView.addView(view);
        synchronized (allVisibleSprites) {
            allVisibleSprites.add(this);
        }
        syncView();
    }

    public void move(double speed, double angle) {
        this.dx.set(speed * Math.cos(angle * Math.PI / 180));
        this.dy.set(speed * Math.sin(angle * Math.PI / 180));
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
        view.setRotation(-rotation.get().floatValue());
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = Math.round((float) (environment.scale * size));
        if (params.height != params.width) {
            params.height = params.width;
            view.requestLayout();
        }
        if (face.get() != lastFace) {
            lastFace = face.get();
            view.setImageDrawable(new Emoji(lastFace).getDrawable(view.getContext()));
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
        collisions.invalidate();
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

            visible.invalidate();
        }
        double rotationSpeedVaue = rotationSpeed.get();
        if (rotationSpeedVaue != 0) {
            double rotationValue = rotation.get() + rotationSpeedVaue;
            while (rotationValue > 360) {
                rotationValue -= 360;
            }
            while (rotationValue < -360) {
                rotationValue += 360;
            }
            rotation.set(rotationValue);
        }
    }

    public void delete() {
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
        synchronized (environment.ticking) {
            environment.ticking.remove(this);
        }
        synchronized (allVisibleSprites) {
            allVisibleSprites.remove(this);
        }
    }


    class VisualProperty<T> extends MutableProperty<T> {

        public VisualProperty(T value) {
            super(value);
        }


        @Override
        public void set(T newValue) {
            super.set(newValue);
            syncView();
        }
    }

}
