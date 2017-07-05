package org.kobjects.codechat.android;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.TupleInstance;
import org.kobjects.codechat.lang.NativeFunction;
import org.kobjects.codechat.type.EnumType;
import org.kobjects.codechat.type.Type;

public class AndroidEnvironment extends Environment implements Runnable {
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    Handler handler = new Handler();
    public Screen screen = new Screen();
    public double scale;
    public static EnumType VerticalAlignment = new EnumType("VerticalAlignment", "TOP", "CENTER", "BOTTOM");
    public static EnumType HorizontalAlignment = new EnumType("HorizontalAlignment", "LEFT", "CENTER", "RIGHT");

    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        handler.postDelayed(this, 100);

        addSystemVariable("screen", screen);
        addSystemVariable("sensors", new Sensors(rootView.getContext()));
        addType(Sprite.TYPE);
        addType(Text.TYPE);
        addType(HorizontalAlignment);
        addType(VerticalAlignment);

        addFunction("move", new NativeFunction(Type.VOID, Sprite.TYPE, Type.NUMBER, Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                ((Sprite) params[0]).move((Double) params[1], (Double) params[2]);
                return null;
            }
        });
    }

    @Override
    public void run() {
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        screen.update(width, height);
        float newScale = Math.min(rootView.getWidth(), rootView.getHeight()) / 1000f;
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


    @Override
    public void clearAll() {
        screen.frame.removeAllListeners();
        if (ticking != null) {
            synchronized (ticking) {
                ticking.clear();
            }
        }
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
