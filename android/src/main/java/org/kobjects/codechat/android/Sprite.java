package org.kobjects.codechat.android;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.LazyProperty;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.lang.Settable;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class Sprite extends TupleInstance implements Ticking, Runnable {
    public final static TupleType TYPE = new TupleType("Sprite") {
        @Override
        public Sprite createInstance(Environment environment, int id) {
            return new Sprite(environment, id);
        }
    };
    static {
        TYPE.addProperty(0, "size", Type.NUMBER, true);
        TYPE.addProperty(1, "x", Type.NUMBER, true);
        TYPE.addProperty(2, "y", Type.NUMBER, true);
        TYPE.addProperty(3, "angle", Type.NUMBER, true);
        TYPE.addProperty(4, "face", Type.STRING, true);
        TYPE.addProperty(5, "collisions", new SetType(Sprite.TYPE), true);
        TYPE.addProperty(6, "dx", Type.NUMBER, true);
        TYPE.addProperty(7, "dy", Type.NUMBER, true);
        TYPE.addProperty(8, "rotation", Type.NUMBER, true);
        TYPE.addProperty(9, "touched", Type.BOOLEAN, false);
        TYPE.addProperty(10, "direction", Type.NUMBER, true);
        TYPE.addProperty(11, "speed", Type.NUMBER, true);
        TYPE.addProperty(12, "visible", Type.BOOLEAN, false);
        TYPE.addProperty(13, "horizontalAlignment", AndroidEnvironment.HorizontalAlignment, true);
        TYPE.addProperty(14, "verticalAlignment", AndroidEnvironment.VerticalAlignment, true);
    }

    private final ImageView view;
    private boolean syncRequested;
    private static List<Sprite> allVisibleSprites = new ArrayList();
    AndroidEnvironment environment;

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(100.0);
    public VisualMaterialProperty<Double> x = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> y = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<Double> angle = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<EnumLiteral> horizonalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.HorizontalAlignment.getValue("CENTER"));
    public VisualMaterialProperty<EnumLiteral> verticalAlignment = new VisualMaterialProperty<>(AndroidEnvironment.VerticalAlignment.getValue("CENTER"));
    public VisualMaterialProperty<String> face = new VisualMaterialProperty<>(new String(Character.toChars(0x1f603)));
    public LazyProperty<Collection> collisions = new LazyProperty<Collection>() {
        @Override
        protected Collection compute() {
            LinkedHashSet<Object> result = new LinkedHashSet<>();
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
            return new Collection(new SetType(Sprite.TYPE), result);
        }
    };

    public MaterialProperty<Double> dx = new MaterialProperty<>(0.0);
    public MaterialProperty<Double> dy = new MaterialProperty<>(0.0);
    public MaterialProperty<Double> rotation = new MaterialProperty<>(0.0);
    public MaterialProperty<Boolean> touched = new MaterialProperty<>(false);

    public SettableProperty<Double> direction = new SettableProperty<Double>() {
        @Override
        public Double get() {
            return Math.atan2(dy.get(), dx.get());
        }

        @Override
        public void set(Double value) {
            move(speed.get(), value);
        }
    };

    public SettableProperty<Double> speed = new SettableProperty<Double>() {
        @Override
        public Double get() {
            double dX = dx.get();
            double dY = dy.get();
            return Math.sqrt(dX * dX + dY * dY);
        }

        @Override
        public void set(Double value) {
            move(value, direction.get());
        }
    };

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
        this.dx.set(speed * Math.cos(angle));
        this.dy.set(speed * Math.sin(angle));
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
        switch (horizonalAlignment.get().getName()) {
            case "LEFT":
                view.setX((float) (environment.scale * (x.get())));
                break;
            case "CENTER":
                view.setX((float) (environment.rootView.getMeasuredWidth()/2 + environment.scale * (x.get() - size / 2)));
                break;
            case "RIGHT":
                view.setX((float) (environment.rootView.getMeasuredWidth() - environment.scale * (x.get() + size)));
                break;
        }

        switch (verticalAlignment.get().getName()) {
            case "TOP":
                view.setY((float) (environment.scale * (y.get())));
                break;
            case "CENTER":
                view.setY(environment.rootView.getMeasuredHeight() / 2 - (float) (environment.scale * (y.get() + size / 2)));
                break;
            case "BOTTOM":
                view.setY(environment.rootView.getMeasuredHeight() - (float) (environment.scale * (y.get() + size)));
                break;
        }

        view.setRotation((float) (-angle.get() * 180 / Math.PI));
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
    public void tick(double s, boolean force) {
        double dxValue = dx.get() * s;
        double dyValue = dy.get() * s;

        collisions.invalidate();
        if (force || dxValue != 0 || dyValue != 0) {
            Screen screen = environment.screen;
            double xValue = x.get();
            double yValue = y.get();
            double sizeValue = size.get();

            if (dxValue != 0 || force) {
                double xMin;
                double xMax;
                switch (horizonalAlignment.get().getName()) {
                    case "LEFT":
                    case "RIGHT":
                        xMin = -sizeValue * 3 / 2;
                        xMax = screen.width.get() + sizeValue * 3 / 2;
                        break;
                    default:
                        xMin = screen.left.get() - sizeValue;
                        xMax = screen.right.get() + sizeValue;
                        break;
                }
                // System.out.println("xMin: " + xMin + " xMax: " + xMax + " x: " + x.get() + " size: " + size.get() + " screenWidth: " + screen.width.get());
                if (dxValue > 0 && xValue > xMax) {
                    x.set(xMin);
                } else if (dxValue < 0 && xValue < xMin) {
                    x.set(xMax);
                } else {
                    x.set(xValue + dxValue);
                }
            }

            if (dyValue != 0 || force) {
                double yMin;
                double yMax;
                switch (verticalAlignment.get().getName()) {
                    case "TOP":
                    case "BOTTOM":
                        yMin = -sizeValue * 3 / 2;
                        yMax = screen.height.get() + sizeValue * 3 / 2;
                        break;
                    default:
                        yMin = screen.bottom.get() - sizeValue;
                        yMax = screen.top.get() + sizeValue;
                        break;
                }
                if (dyValue > 0 && yValue > yMax) {
                    y.set(yMin);
                } else if (dyValue < 0 && yValue < yMin) {
                    y.set(yMax);
                } else {
                    y.set(yValue + dyValue);
                }
            }

            visible.invalidate();
        }
        double rotationSpeedVaue = rotation.get();
        if (rotationSpeedVaue != 0) {
            double rotationValue = angle.get() + rotationSpeedVaue * s;
            while (rotationValue > 2 * Math.PI) {
                rotationValue -= 2 * Math.PI;
            }
            while (rotationValue < -2 * Math.PI) {
                rotationValue += 2 * Math.PI;
            }
            angle.set(rotationValue);
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

    @Override
    public TupleType getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 0: return size;
            case 1: return x;
            case 2: return y;
            case 3: return angle;
            case 4: return face;
            case 5: return collisions;
            case 6: return dx;
            case 7: return dy;
            case 8: return rotation;
            case 9: return touched;
            case 10: return direction;
            case 11: return speed;
            case 12: return visible;
            case 13: return horizonalAlignment;
            case 14: return verticalAlignment;
            default:
                throw new IllegalArgumentException();
        }
    }


    abstract class SettableProperty<T> extends Property<T> implements Settable<T> {

    }

    class VisualMaterialProperty<T> extends MaterialProperty<T> {

        public VisualMaterialProperty(T value) {
            super(value);
        }


        @Override
        public void set(T newValue) {
            super.set(newValue);
            syncView();
        }
    }

}
