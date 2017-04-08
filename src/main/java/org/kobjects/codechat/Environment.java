package org.kobjects.codechat;

import android.widget.FrameLayout;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Environment {

    FrameLayout rootView;
    public Map<String, Object> variables = new TreeMap<>();

    Environment(FrameLayout rootView) {
        this.rootView = rootView;
    }

    public Object instantiate(Class type) {
        try {
            return type.getConstructor(Environment.class).newInstance(this);
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
}
