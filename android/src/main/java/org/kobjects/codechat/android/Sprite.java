package org.kobjects.codechat.android;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import org.kobjects.codechat.annotation.AnnotatedCharSequence;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.LazyProperty;
import org.kobjects.codechat.lang.MaterialProperty;
import org.kobjects.codechat.lang.Property;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class Sprite extends AbstractViewWrapper<ImageView> implements Ticking, Runnable {
    public final static InstanceType TYPE = new ViewWrapperType<Sprite>() {
        @Override
        public Sprite createInstance(Environment environment, int id) {
            return new Sprite((AndroidEnvironment) environment, id);
        }

        @Override
        public String getName() {
            return "Sprite";
        }

        public AnnotatedCharSequence getDocumentation() {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            asb.append("A sprite is an emoji displayed on a particular position on the screen. "
                            + "It is able to move and rotate at a given speed by setting the corresponding properties.");
            asb.append(super.getDocumentation());
            return asb.build();
        }
    };
    static {
        TYPE.addProperty(4, "size", Type.NUMBER, true,
                "The size of this sprite.");
        TYPE.addProperty(5, "angle", Type.NUMBER, true,
                "The clockwise rotation angle of this sprite in degree.");
        TYPE.addProperty(6, "face", Type.STRING, true, "The emoji displayed for this sprite.");
        TYPE.addProperty(7, "collisions", new SetType(Sprite.TYPE), true,
                "The set of other sprites this sprite is currently colliding with");
        TYPE.addProperty(8, "dx", Type.NUMBER, true,
                "The current horizontal speed of this sprite in units per second.");
        TYPE.addProperty(9, "dy", Type.NUMBER, true,
                "The current vertical speed of this sprite in units per second.");
        TYPE.addProperty(10, "rotation", Type.NUMBER, true,
                "The current clockwise rotation speed in degree per second.");
        TYPE.addProperty(11, "touch", Type.BOOLEAN, false,
                "True if this sprite is currently touched.");
        TYPE.addProperty(12, "direction", Type.NUMBER, true,
                "The movement direction of this sprite in degree; 0 if the sprite is not moving.");
        TYPE.addProperty(13, "speed", Type.NUMBER, true,
                "The current speed in units per second.");
        TYPE.addProperty(14, "visible", Type.BOOLEAN, false,
                "True if the sprite is currently withing the usable screen boundaries.");
        TYPE.addProperty(15, "edgeMode", AndroidEnvironment.EdgeMode.TYPE, true,
                "Determines behavior when the sprite hits the edge of the screen.");
    }

    static List<WeakReference<Sprite>> allSprites = new ArrayList();

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<Double> angle = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<String> face = new VisualMaterialProperty<>(new String(Character.toChars(0x1f603)));
    public LazyProperty<Collection> collisions = new LazyProperty<Collection>() {
        @Override
        protected Collection compute() {
            Collection result = (Collection) environment.instantiate(new SetType(Sprite.TYPE), -1);

            double x = Sprite.this.x.get();
            double y = Sprite.this.y.get();
            double size = Sprite.this.size.get();
            synchronized (allSprites) {
                for (WeakReference<Sprite> otherRef : allSprites) {
                    Sprite other = otherRef.get();
                    if (other != null && other != Sprite.this) {
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

    public MaterialProperty<Double> dx = new MaterialProperty<>(0.0);
    public MaterialProperty<Double> dy = new MaterialProperty<>(0.0);
    public MaterialProperty<Double> rotation = new MaterialProperty<>(0.0);
    public MaterialProperty<Boolean> touch = new MaterialProperty<>(false);
    public MaterialProperty<AndroidEnvironment.EdgeMode> edgeMode = new MaterialProperty<>(AndroidEnvironment.EdgeMode.NONE);

    public Property<Double> direction = new Property<Double>() {
        @Override
        public Double get() {
            return Math.atan2(dy.get(), dx.get()) * 180 / Math.PI;
        }

        @Override
        public void set(Double value) {
            move(speed.get(), value);
        }
    };

    public Property<Double> speed = new Property<Double>() {
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
            return view.getParent() != null;
        }
    };

    private String lastFace;

    public Sprite(AndroidEnvironment environment, int id) {
        super(environment, id, new ImageView(environment.rootView.getContext()));
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // view.setDrawingCacheEnabled(true);
        view.setTag(this);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    touch.set(true);
                    return true;
                }
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    touch.set(false);
                    return true;
                }
                return false;
            }
        });
        synchronized (allSprites) {
            allSprites.add(new WeakReference(this));
        }
        syncView();
    }

    private static double toRad(double deg) {
        return -deg * Math.PI / 180;
    }

    public void move(double speed, double angle) {
        this.dx.set(speed * Math.cos(toRad(angle)));
        this.dy.set(speed * Math.sin(toRad(angle)));
    }


    public void syncView() {
        if (!syncRequested) {
            syncRequested = true;
            ((Activity) environment.rootView.getContext()).runOnUiThread(this);
        }
    }

    public void run() {
        syncRequested = false;
        if (detached) {
            if (view.getParent() != null) {
                environment.rootView.removeView(view);
            }
            return;
        }
        int screenWidth = environment.rootView.getMeasuredWidth();
        int screenHeight = environment.rootView.getMeasuredHeight();
        double size = this.size.get();
        double scale = environment.scale;
        double scaledSize = scale * size;
        float scaledX;
        switch (xAlign.get()) {
            case LEFT:
                scaledX = (float) (scale * x.get());
                break;
            case RIGHT:
                scaledX = ((float) (screenWidth - scale * (x.get() + size)));
                break;
            default:
                scaledX = ((float) (screenWidth/2 + scale * (x.get() - size / 2)));
                break;
        }
        float scaledY;
        switch (yAlign.get()) {
            case TOP:
                scaledY = (float) (scale * (y.get()));
                break;
            case BOTTOM:
                scaledY = screenHeight - (float) (environment.scale * (y.get() + size));
                break;
            default:
                scaledY = (screenHeight / 2 - (float) (environment.scale * (y.get() + size / 2)));
                break;
        }

        if (scaledX >= -scaledSize && scaledY >= -scaledSize && scaledX <= screenWidth && scaledY <= screenHeight) {
            if (view.getParent() == null) {
                environment.rootView.addView(view);
            }
            view.setX(scaledX);
            view.setY(scaledY);
            view.setRotation(angle.get().floatValue());
            ViewGroup.LayoutParams params = view.getLayoutParams();
            params.width = Math.round((float) (environment.scale * size));
            if (params.height != params.width) {
                params.height = params.width;
                view.requestLayout();
            }
            if (face.get() != lastFace) {
                lastFace = face.get();
                view.setImageDrawable(new EmojiDrawable(lastFace));
            }
        } else if (view.getParent() != null) {
            environment.rootView.removeView(view);
        }
    }

/*
    public Emoji getFace() {
        return new Emoji(view.getText().toString());
    }
*/
    private static double wrapValue(double position, double size, double delta, double max, EnumLiteral align) {
        if (delta > 0) {
            if ("CENTER".equals(align.name())) {
                if (position + delta > (max + size) / 2) {
                    return -(max + size) / 2;
                }
            } else {
                if (position + delta > max) {
                    return -size;
                }
            }
        } else if (delta < 0) {
            if ("CENTER".equals(align.name())) {
                if (position + delta < -(max + size) / 2) {
                    return (max + size) / 2;
                }
            } else {
                if (position + delta < -size) {
                    return max;
                }
            }
        }
        return position + delta;
    }

    private static boolean needsBounce(double position, double size, double delta, double max, EnumLiteral align) {
        if (delta > 0) {
            if ("CENTER".equals(align.name())) {
                return (position + delta > (max - size) / 2);
            }
            return (position + delta + size > max);
        }
        if (delta < 0) {
            if ("CENTER".equals(align.name())) {
                return (position + delta < -(max - size) / 2);
            }
            return position + delta < 0;
        }
        return false;
    }


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
                switch (edgeMode.get()) {
                    case WRAP:
                        x.set(wrapValue(xValue, sizeValue, dxValue, screen.width.get(), xAlign.get()));
                        break;
                    case BOUNCE:
                        if (needsBounce(xValue, sizeValue, dxValue, screen.width.get(), xAlign.get())) {
                            dx.set(-dx.get());
                        }
                        x.set(xValue + dxValue);
                        break;
                    default:
                        x.set(xValue + dxValue);
                        break;
                }
            }

            if (dyValue != 0 || force) {
                switch (edgeMode.get()) {
                    case WRAP:
                        y.set(wrapValue(yValue, sizeValue, dyValue, screen.height.get(), yAlign.get()));
                        break;
                    case BOUNCE:
                        if (needsBounce(yValue, sizeValue, dyValue, screen.height.get(), yAlign.get())) {
                            dy.set(-dy.get());
                        }
                        y.set(yValue + dyValue);
                        break;
                    default:
                        y.set(yValue + dyValue);
                        break;
                }
            }

            visible.invalidate();
        }
        double rotationSpeedVaue = rotation.get();
        if (rotationSpeedVaue != 0) {
            double rotationValue = angle.get() + rotationSpeedVaue * s;
            while (rotationValue > 360) {
                rotationValue -= 360;
            }
            while (rotationValue < -360) {
                rotationValue += 360;
            }
            angle.set(rotationValue);
        }
    }

    public void delete() {
       super.delete();
        synchronized (allSprites) {
            allSprites.remove(this);
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
            case 5: return angle;
            case 6: return face;
            case 7: return collisions;
            case 8: return dx;
            case 9: return dy;
            case 10: return rotation;
            case 11: return touch;
            case 12: return direction;
            case 13: return speed;
            case 14: return visible;
            case 15: return edgeMode;
            default:
                return super.getProperty(index);
        }
    }


    public static void clearAll() {
        allSprites.clear();
    }
}
