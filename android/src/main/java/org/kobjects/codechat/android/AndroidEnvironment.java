package org.kobjects.codechat.android;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
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
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    Handler handler = new Handler();
    public Screen screen = new Screen();
    public double scale;
    public static EnumType YAlign = new EnumType("YAlign", "TOP", "CENTER", "BOTTOM");
    public static EnumType XAlign = new EnumType("XAlign", "LEFT", "CENTER", "RIGHT");

    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
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
