package org.kobjects.codechat;

import android.os.Handler;
import android.widget.FrameLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Environment implements Runnable {

    double scale;
    FrameLayout rootView;
    public Map<String, Object> variables = new TreeMap<>();
    List<Ticking> ticking = new ArrayList<>();
    Handler handler = new Handler();

    public Environment(FrameLayout rootView) {
        this.rootView = rootView;
        handler.postDelayed(this, 100);
    }

    public Object instantiate(Class type) {
        try {
            Object o = type.getConstructor(Environment.class).newInstance(this);
            if (o instanceof Ticking) {
                ticking.add((Ticking) o);
            }
            return o;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        float min = Math.min(rootView.getMeasuredWidth(), rootView.getMeasuredHeight());
        float newScale = min / 1000;
        boolean force = newScale != scale;
        scale = newScale;
        for (Ticking t : ticking) {
            t.tick(force);
        }
        handler.postDelayed(this, 30);
    }
}
