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
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.NativeFunction;
import org.kobjects.codechat.lang.SerializationContext;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.TupleType;
import org.kobjects.codechat.type.Type;

public class AndroidEnvironment extends Environment implements Runnable {
    private static final String[] SOUND_EXTENSIONS = {".mp3", ".wav"};
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    public Screen screen = new Screen();
    public double scale;
    public static EnumType YAlign = new EnumType("YAlign", "TOP", "CENTER", "BOTTOM");
    public static EnumType XAlign = new EnumType("XAlign", "LEFT", "CENTER", "RIGHT");
    public static EnumType ScreenBounds = new EnumType("ScreenBounds", "NONE", "BOUNCE", "WRAP");
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
        addType(XAlign);
        addType(YAlign);
        addType(ScreenBounds);

        addType(Sensors.TYPE);

        addSystemVariable("screen", screen);
        addSystemVariable("sensors", new Sensors(rootView.getContext()));

        addNativeFunction(new NativeFunction("move", Type.VOID,
                "Sets the speed and direction for the given sprite", Sprite.TYPE, Type.NUMBER, Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                ((Sprite) params[0]).move((Double) params[1], (Double) params[2]);
                return null;
            }
        });
        addNativeFunction(new NativeFunction("play", Type.VOID,  "Plays the given sound", Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                new Sound(soundManager, (String) params[0]).play();
                return null;
            }
        });

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



    protected void addExtraRootEntities(SerializationContext serializationContext) {
        super.addExtraRootEntities(serializationContext);
        for (WeakReference<Sprite> spriteRef : Sprite.allSprites) {
            Sprite sprite = spriteRef.get();
            if (sprite != null) {
                serializationContext.enqueue(sprite);
            }
        }
    }


    @Override
    public void clearAll() {
        for (TupleType.PropertyDescriptor propertyDescriptor : screen.getType().properties()) {
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
