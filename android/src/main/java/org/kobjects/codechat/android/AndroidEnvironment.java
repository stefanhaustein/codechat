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

    void playSound(final String s, int pos, boolean mayBlock) {
        while (pos < s.length()) {
            final int codePoint = Character.codePointAt(s, pos);
            pos += Character.charCount(codePoint);
            final double f;
            switch (codePoint) {
                case 'C': f = 261.63; break;
                case 'D': f = 293.66; break;
                case 'E': f = 329.63; break;
                case 'F': f = 349.23; break;
                case 'G': f = 392; break;
                case 'A': f = 440; break;
                case 'B': f = 493.88; break;
                case 'c': f = 523.25; break;
                case 'd': f = 587.33; break;
                case 'e': f = 659.26; break;
                case 'f': f = 698.46; break;
                case 'g': f = 783.99; break;
                case 'a': f = 880; break;
                case 'b': f = 987.77; break;
                default:
                    try {
                        String name = Integer.toHexString(codePoint);
                        if (sounds.contains(name + ".mp3")) {
                            name += ".mp3";
                        } else if (sounds.contains(name + ".wav")) {
                            name += ".wav";
                        } else {
                            continue;
                        }
                        AssetFileDescriptor descriptor = rootView.getContext().getAssets().openFd("sound/" + name);
                        final MediaPlayer m = new MediaPlayer();
                        m.setDataSource(descriptor.getFileDescriptor(), descriptor.getStartOffset(), descriptor.getLength());
                        descriptor.close();
                        final int nextPos = pos;
                        m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            @Override
                            public void onCompletion(MediaPlayer mediaPlayer) {
                                m.release();
                                playSound(s, nextPos, false);
                            }
                        });

                        m.prepare();
                        m.setVolume(1f, 1f);
                        m.setLooping(false);
                        m.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
            }

            double duration = 0.25;
            if (pos < s.length()) {
                char c = s.charAt(pos);
                boolean divide = false;
                if (c == '/') {
                    pos++;
                    divide = true;
                }
                int len = 0;
                while (pos < s.length() && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
                    len = len * 10 + (s.charAt(pos++) - '0');
                }
                if (divide) {
                    duration /= len == 0 ? 2 : len;
                } else if (len != 0) {
                    duration *= len;
                }
            }
            if (mayBlock) {
                Tone tone = new Tone(f, duration);
                tone.play();
            } else {
                final int finalPos = pos;
                final double finalDuration = duration;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Tone tone = new Tone(f, finalDuration);
                        tone.play();
                        playSound(s, finalPos, true);
                    }
                }).start();
                return;
            }
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
                playSound((String) params[0], 0, false);
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
