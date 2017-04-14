package org.kobjects.codechat;

import android.os.Handler;
import android.widget.FrameLayout;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class Environment implements Runnable {

    static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                default:
                    if (c < ' ') {
                        sb.append("\\u00");
                        sb.append(Character.digit(c / 16, 16));
                        sb.append(Character.digit(c % 16, 16));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }


    double scale;
    FrameLayout rootView;
    public Map<String, Object> variables = new TreeMap<>();
    List<Ticking> ticking = new ArrayList<>();
    Handler handler = new Handler();
    int lastId;
    Map<Integer,Instance> everything = new TreeMap<>();

    public Environment(FrameLayout rootView) {
        this.rootView = rootView;
        handler.postDelayed(this, 100);
    }

    public Object instantiate(Class type) {
        try {
            int instanceId = ++lastId;
            Instance instance = (Instance) type.getConstructor(Environment.class, Integer.TYPE).newInstance(this, instanceId);
            everything.put(instanceId, instance);
            if (instance instanceof Ticking) {
                ticking.add((Ticking) instance);
            }
            return instance;
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
        float newScale = rootView.getWidth() / 1000f;
        boolean force = newScale != scale;
        scale = newScale;
        for (Ticking t : ticking) {
            try {
                t.tick(force);
            } catch (Exception e) {
                System.err.println(e.toString());
            }
        }
        handler.postDelayed(this, 17);
    }


    public void dump(Writer writer) throws IOException {
        for (Instance instance : everything.values()) {
           instance.dump(writer);
        }
        for (Map.Entry<String,Object> var : variables.entrySet()) {
            if (var.getValue() instanceof Class<?>) {
                continue;
            }
            writer.write(var.getKey());
            writer.write(" = ");
            writer.write(var.getValue().toString());
            writer.write("\n");
        }

    }


    public String toLiteral(Object o) {
        if (o instanceof Number) {
            Number n = (Number) o;
            if (n.longValue() == n.doubleValue()) {
                return String.valueOf(n.longValue());
            }
        }
        if (o instanceof String) {
            return quote((String) o);
        }
        return String.valueOf(o);
    }


}
