package org.kobjects.codechat.android;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.kobjects.codechat.android.sound.Sound;
import org.kobjects.codechat.android.sound.SampleManager;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.NativeFunction;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

public class AndroidEnvironment extends Environment implements Runnable {

    enum XAlign implements EnumLiteral {
        LEFT, CENTER, RIGHT;
        public static EnumType TYPE = new EnumType("XAlign", LEFT, CENTER, RIGHT);
        @Override
        public Type getType() {
            return TYPE;
        }
    };

    enum YAlign implements EnumLiteral {
        TOP, CENTER, BOTTOM;
        public static EnumType TYPE = new EnumType("YAlign", TOP, CENTER, BOTTOM);
        @Override
        public Type getType() {
            return TYPE;
        }
    };

    enum EdgeMode implements EnumLiteral {
        NONE, BOUNCE, WRAP;
        public static EnumType TYPE = new EnumType("EdgeMode", NONE, BOUNCE, WRAP);
        @Override
        public Type getType() {
            return TYPE;
        }
    };

    private static final String[] SOUND_EXTENSIONS = {".mp3", ".wav"};
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    public Screen screen = new Screen(this);
    public double scale;
    Handler handler = new Handler();
    final Context context;


    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        this.context = rootView.getContext();
        handler.postDelayed(this, 100);
        final SampleManager soundManager = new SampleManager(context);

        addType(Screen.TYPE);
        addType(Sprite.TYPE);
        addType(Text.TYPE);
        addType(XAlign.TYPE);
        addType(YAlign.TYPE);
        addType(EdgeMode.TYPE);

        addType(Sensors.TYPE);

        addSystemConstant("screen", screen, null);
        addSystemConstant("sensors", new Sensors(this, rootView.getContext()), null);

        addSystemConstant("play", new NativeFunction( null, Type.STRING) {
                    @Override
                    protected Object eval(Object[] params) {
                        new Sound(soundManager, (String) params[0]).play();
                        return null;
                    }
                }, "Plays the given sound");

    }

    @Override
    public void run() {
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        screen.update(width, height);
        float newScale = Math.min(rootView.getWidth(), rootView.getHeight()) / 100f;
        boolean force = newScale != scale;
        scale = newScale;
        if (!paused || force) {
            List<Sprite> copy = new ArrayList<>();
            synchronized (Sprite.allSprites) {
                for (WeakReference<Sprite> ref : Sprite.allSprites) {
                    Sprite sprite = ref.get();
                    if (sprite != null) {
                        copy.add(sprite);
                    }
                }
            }
            for (Sprite sprite : copy) {
                try {
                    sprite.tick(0.017, force);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            screen.frame.set(screen.frame.get() + 1);
        }
        // TODO: Use Choreographer instead?
        handler.postDelayed(this, 17);
    }

    @Override
    public void clearAll() {
        for (InstanceType.PropertyDescriptor propertyDescriptor : screen.getType().properties()) {
            screen.getProperty(propertyDescriptor.index).removeAllListeners();
        }
        Sprite.clearAll();
        super.clearAll();
        if (rootView != null) {
            for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
                View child = rootView.getChildAt(i);
                if (child instanceof ImageView) {
                    rootView.removeViewAt(i);
                }
            }
        }
    }
}
