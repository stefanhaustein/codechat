package org.kobjects.codechat.android.api.ui;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vanniktech.emoji.EmojiRange;
import com.vanniktech.emoji.EmojiUtils;
import com.vanniktech.emoji.emoji.Emoji;
import java.util.List;
import org.kobjects.codechat.android.AndroidEnvironment;
import org.kobjects.codechat.android.Ticking;
import org.kobjects.codechat.lang.Collection;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.instance.LazyProperty;
import org.kobjects.codechat.instance.MaterialProperty;
import org.kobjects.codechat.instance.Property;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.Type;


public class Sprite extends AbstractViewWrapper<ImageView> implements Ticking, Runnable {
    public final static Classifier TYPE = new ViewWrapperType<Sprite>() {
        @Override
        public String getName() {
            return "Sprite";
        }

        @Override
        public Sprite createInstance(Environment environment) {
            return new Sprite((AndroidEnvironment) environment);
        }
    };
    static {
        TYPE.addProperty(6, "size", Type.NUMBER, true,
                "The size of this sprite.\n\nExample:\n\n`let bob = new Sprite\nbob.size = 25`");
        TYPE.addProperty(7, "angle", Type.NUMBER, true,
                "The clockwise rotation angle of this sprite in degree.\n\nExample:\n\n`let bob := new Sprite\nbob.angle := 180`");
        TYPE.addProperty(8, "face", Type.STRING, true, "The emoji displayed for this sprite.");
        TYPE.addProperty(9, "collisions", new SetType(Sprite.TYPE), false,
                "The set of other sprites this sprite is currently colliding with");
        TYPE.addProperty(10, "dx", Type.NUMBER, true,
                "The current horizontal speed of this sprite in units per second.\n\nExample:\n\n`let bob := new Sprite\nbob.dx := 10`");
        TYPE.addProperty(11, "dy", Type.NUMBER, true,
                "The current vertical speed of this sprite in units per second.\n\nExample:\n\n`let bob := new Sprite\nbob.dy := 10`");
        TYPE.addProperty(12, "rotation", Type.NUMBER, true,
                "The current clockwise rotation speed in degree per second.\n\nExample:\n\n`let bob := new Sprite\nbob.rotation := 180`");
        TYPE.addProperty(13, "touch", Type.BOOLEAN, false,
                "True if this sprite is currently touched.\n\nExample: `Mole`");
        TYPE.addProperty(14, "direction", Type.NUMBER, true,
                "The movement direction of this sprite in degree; 0 if the sprite is not moving.");
        TYPE.addProperty(15, "speed", Type.NUMBER, true,
                "The current speed in units per second.");
        TYPE.addProperty(16, "visible", Type.BOOLEAN, false,
                "True if the sprite is currently within the screen boundaries.");
        TYPE.addProperty(17, "edgeMode", EdgeMode.TYPE, true,
                "Determines behavior when the sprite hits the edge of the screen. Example: `EdgeMode`");
        TYPE.addProperty(18, "grow", Type.NUMBER, true,
                "The current growth in units per second. Use negative numbers to shrink");
        TYPE.addProperty(19, "fade", Type.NUMBER, true,
                "The current fading per second. Use negative numbers to fade out.");
    }

    public VisualMaterialProperty<Double> size = new VisualMaterialProperty<>(10.0);
    public VisualMaterialProperty<Double> angle = new VisualMaterialProperty<>(0.0);
    public VisualMaterialProperty<String> face = new VisualMaterialProperty<>(new String(Character.toChars(0x1f603)));
    public LazyProperty<Collection> collisions = new LazyProperty<Collection>() {
        @Override
        protected Collection compute() {
            Collection result = (Collection) environment.createInstance(new SetType(Sprite.TYPE), -1);

            double size = Sprite.this.size.get() * 0.8;
            double virtualScreenWidth = environment.screen.width.get();
            double virtualScreenHeight = environment.screen.height.get();
            double x = Sprite.this.getNormalizedX(virtualScreenWidth) + size / 2;
            double y = Sprite.this.getNormalizedY(virtualScreenHeight) + size / 2;
            for (Object o: environment.screen.sprites.get()) {
                Sprite other = (Sprite) o;
                if (other != null && other != Sprite.this && other.view.getParent() != null) {
                    double otherSize = other.size.get() * 0.8;
                    double distX = other.getNormalizedX(virtualScreenWidth) + otherSize / 2 - x;
                    double distY = other.getNormalizedY(virtualScreenHeight) + otherSize / 2 - y;
                    double minDist = (other.size.get() + size) / 2;
                    if (distX * distX + distY * distY < minDist * minDist) {
                        result.add(other);
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
    public MaterialProperty<EdgeMode> edgeMode = new MaterialProperty<>(EdgeMode.NONE);
    public MaterialProperty<Double> grow = new MaterialProperty<>(0.0);
    public MaterialProperty<Double> fade = new MaterialProperty<>(0.0);
    private int viewIndexCache;

    public Property<Double> direction = new Property<Double>() {
        @Override
        public Double get() {
            return - Math.atan2(dy.get(), dx.get()) * 180 / Math.PI;
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

    public Sprite(AndroidEnvironment environment) {
        super(environment, new ImageView(environment.rootView.getContext()));
        view.setAdjustViewBounds(true);
        view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        // view.setDrawingCacheEnabled(true);
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

        environment.screen.addSprite(this);
        syncView();
    }

    private static double toRad(double deg) {
        return deg * Math.PI / 180;
    }

    private void move(double speed, double angle) {
        this.dx.set(speed * Math.cos(-toRad(angle)));
        this.dy.set(speed * Math.sin(-toRad(angle)));
    }



    public void run() {
        syncRequested = false;

        double size = this.size.get();
        double scale = environment.scale;
        double virtualScreenWidth = environment.screen.width.get();
        double virtualScreenHeight = environment.screen.height.get();

        double normalizedX = getNormalizedX(virtualScreenWidth);
        double normalizedY = getNormalizedY(virtualScreenHeight);

        float scaledX = (float) (normalizedX * scale);
        float scaledY = (float) (normalizedY * scale);

      //  view.setZ(z.get().floatValue());
        view.setAlpha(opacity.get().floatValue());

        if (normalizedX + size > -50 && normalizedY + size > -50 && normalizedX < virtualScreenWidth + 50 && normalizedY < virtualScreenHeight + 50 &&
                opacity.get() > 0 && size > 0) {
            if (view.getParent() == null) {
                environment.rootView.addView(view);
                environment.screen.sprites.invalidate();
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

                List<EmojiRange> emojis = EmojiUtils.emojis(lastFace);
                if (emojis.size() > 0) {
                    Emoji emoji = emojis.get(0).emoji;
                    view.setImageDrawable(emoji.getDrawable(view.getContext()));
                }
        //        view.setImageDrawable(new EmojiDrawable(lastFace));
            }

            double zValue = z.get();
            ViewGroup container = (ViewGroup) view.getParent();
            int count = container.getChildCount();
            if (viewIndexCache >= count || container.getChildAt(viewIndexCache) != view) {
                viewIndexCache = 0;
                while (container.getChildAt(viewIndexCache) != view) {
                    viewIndexCache++;
                }
            }

            int newIndex = viewIndexCache;
            while (newIndex > 0 &&
                ((Double) ((AbstractViewWrapper) container.getChildAt(newIndex - 1).getTag()).z.get()).doubleValue() > zValue) {
                newIndex--;
            }
            if (newIndex == viewIndexCache) {
                while (newIndex < count - 1 &&
                    ((Double) ((AbstractViewWrapper) container.getChildAt(newIndex + 1).getTag()).z.get()).doubleValue() < zValue) {
                    newIndex++;
                }
            }
            if (newIndex != viewIndexCache) {
                container.removeViewAt(viewIndexCache);
                container.addView(view, newIndex);
                viewIndexCache = newIndex;
            }


        } else if (view.getParent() != null) {
            // TODO: Wait some cycles before punting...
            environment.rootView.removeView(view);
            environment.screen.sprites.invalidate();
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

        double grow = this.grow.get();
        if (grow != 0) {
            size.set(size.get() + s * grow);
        }
        double fade = this.fade.get();
        if (fade != 0) {
            opacity.set(opacity.get() + s * fade);
        }
    }


    @Override
    public Classifier getType() {
        return TYPE;
    }

    @Override
    public Property getProperty(int index) {
        switch (index) {
            case 6: return size;
            case 7: return angle;
            case 8: return face;
            case 9: return collisions;
            case 10: return dx;
            case 11: return dy;
            case 12: return rotation;
            case 13: return touch;
            case 14: return direction;
            case 15: return speed;
            case 16: return visible;
            case 17: return edgeMode;
            case 18: return grow;
            case 19: return fade;
            default:
                return super.getProperty(index);
        }
    }

    @Override
    public double getWidth() {
        return size.get();
    }

    @Override
    public double getHeight() {
        return size.get();
    }

}
