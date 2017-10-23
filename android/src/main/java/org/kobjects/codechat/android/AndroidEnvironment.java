package org.kobjects.codechat.android;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Instance;
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
    Handler handler = new Handler();
    final Context context;
    private HashSet<String> sounds = new HashSet<String>();

    void playSound(String s) {
        try {
            String name = Integer.toHexString(Character.codePointAt(s,0));
            if (sounds.contains(name + ".mp3")) {
                name += ".mp3";
            } else if (sounds.contains(name + ".wav")) {
                name += ".wav";
            } else {
                throw new RuntimeException("Sound '" + name + "' not found");
            }
            AssetFileDescriptor descriptor = rootView.getContext().getAssets().openFd("sound/" +  name);
            final MediaPlayer m = new MediaPlayer();
            m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
            descriptor.close();

            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    m.release();
                }
            });

            m.prepare();
            m.setVolume(1f, 1f);
            m.setLooping(false);
            m.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        this.context = rootView.getContext();
        handler.postDelayed(this, 100);


        addType(Screen.TYPE);
        addType(Sprite.TYPE);
        addType(Text.TYPE);
        addType(XAlign);
        addType(YAlign);

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
        addNativeFunction(new NativeFunction("playSound", Type.VOID,  "Plays the given sound", Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                playSound((String) params[0]);
                return null;
            }
        });

        try {
            for (String s : context.getAssets().list("sound")) {
                sounds.add(s);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            List<Ticking> copy = new ArrayList<>();
            synchronized (ticking) {
                for (Ticking t : ticking) {
                    copy.add(t);
                }
            }
            for (Ticking t : copy) {
                try {
                    t.tick(0.017, force);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            screen.frame.set(screen.frame.get() + 1);
        }
        handler.postDelayed(this, 17);
    }



    protected void addExtraRootEntities(SerializationContext serializationContext) {
        for (Entity entity : Sprite.allVisibleSprites) {
            serializationContext.enqueue(entity);
        }
    }


    @Override
    public void clearAll() {
        for (TupleType.PropertyDescriptor propertyDescriptor : screen.getType().properties()) {
            screen.getProperty(propertyDescriptor.index).removeAllListeners();
        }
        if (ticking != null) {
            synchronized (ticking) {
                ticking.clear();
            }
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

    public Instance instantiate(Type type, int id) {
        Instance instance = super.instantiate(type, id);
        if (instance instanceof Ticking) {
            synchronized (ticking) {
                ticking.add((Ticking) instance);
            }
        }
        return instance;
    }
}
