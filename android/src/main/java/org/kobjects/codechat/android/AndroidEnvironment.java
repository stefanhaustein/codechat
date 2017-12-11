package org.kobjects.codechat.android;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.LinkedHashSet;
import org.kobjects.codechat.android.sound.Sound;
import org.kobjects.codechat.android.sound.SampleManager;
import org.kobjects.codechat.lang.EnumLiteral;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.NativeFunction;
import org.kobjects.codechat.type.EnumType;
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
    public Screen screen;
    public double scale;
    Handler handler = new Handler();
    final Context context;


    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        this.context = rootView.getContext();
        this.screen = new Screen(this);
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
        boolean force = screen.update(width, height);
        scale = Math.min(width, height) / 100f;
        if (!suspended) {
           for (Object sprite : screen.sprites.get()) {
                try {
                    ((Sprite) sprite).tick(0.017, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // TODO: Use Choreographer instead?
        handler.postDelayed(this, 17);
    }

    @Override
    public void clearAll() {
        screen.clearAll();
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
