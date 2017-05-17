package org.kobjects.codechat.lang;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import org.kobjects.codechat.api.Ticking;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class AndroidEnvironment extends Environment implements Runnable {
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();

    public AndroidEnvironment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        super(environmentListener, codeDir);
        this.rootView = rootView;
        handler.postDelayed(this, 100);
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
                    t.tick(force);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            screen.frame.set(screen.frame.get() + 1);
        }
        handler.postDelayed(this, 17);
    }


    public void clearAll() {
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

    public Instance instantiate(Class type, int id) {
        Instance instance = super.instantiate(type, id);
        if (instance instanceof Ticking) {
            synchronized (ticking) {
                ticking.add((Ticking) instance);
            }
        }
        return instance;
    }
}
