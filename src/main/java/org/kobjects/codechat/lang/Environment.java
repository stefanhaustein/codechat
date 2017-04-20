package org.kobjects.codechat.lang;

import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.kobjects.codechat.api.Builtins;
import org.kobjects.codechat.api.Sprite;
import org.kobjects.codechat.api.Ticking;
import org.kobjects.expressionparser.ExpressionParser;

public class Environment implements Runnable {

    public static String quote(String s) {
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


    public Builtins builtins = new Builtins(this);
    public double scale;
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    public boolean paused;
    Handler handler = new Handler();
    int lastId;
    Map<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    File rootDir;
    boolean loading;
    EnvironmentListener environmentListener;
    Scope rootScope = new Scope(this);
    Parser parser = new Parser(this);
    private Context rootContext = new Context(this);

    public Environment(EnvironmentListener environmentListener, FrameLayout rootView) {
        this.environmentListener = environmentListener;
        this.rootView = rootView;
        this.rootDir = rootView.getContext().getFilesDir();
        clear();
        handler.postDelayed(this, 100);
    }

    public Instance instantiate(Class type) {
        try {
            int instanceId = ++lastId;
            Instance instance = (Instance) type.getConstructor(Environment.class, Integer.TYPE).newInstance(this, instanceId);
            everything.put(instanceId, new WeakReference<Instance>(instance));
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
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public void run() {
        float newScale = Math.min(rootView.getWidth(), rootView.getHeight()) / 1000f;
        boolean force = newScale != scale;
        scale = newScale;
        if (!paused || force) {
            for (Ticking t : ticking) {
                try {
                    t.tick(force);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        handler.postDelayed(this, 17);
    }


    public void dump(Writer writer) throws IOException {
        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.dump(writer);
            }
        }
        for (Variable var : rootScope.variables.values()) {
            writer.write(var.getName());
            writer.write(" = ");
            writer.write(toLiteral(rootContext.variables[var.getIndex()]));
            writer.write("\n");
        }
        for (Ticking t : ticking) {
            if (!(t instanceof Sprite)) {
                String s = t.toString();
                writer.write(s);
                if (!s.endsWith("\n")) {
                    writer.write("\n");
                }
            }
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

    public int getBalance(String line) {
        ExpressionParser.Tokenizer tokenizer = parser.createTokenizer(line);
        int balance = 0;
        while (true) {
            tokenizer.nextToken();
            switch (tokenizer.currentValue) {
                case "":
                    return balance;
                case "{":
                    balance++;
                    break;
                case "}":
                    balance--;
                    break;
            }
        }
    }


    public Evaluable parse(String line) {
        return parser.parse(line);
    }


    public Object getInstance(Type type, int id) {
        WeakReference reference = everything.get(id);
        Object result = reference != null ? reference.get() : null;
        if (result == null) {
            if (!loading) {
                throw new RuntimeException("Undefined instance reference: " + type + "#" + id);
            }
            Class<?> c = type.getJavaClass();
            result = instantiate(c);
        } else {
            if (!result.getClass().getSimpleName().equalsIgnoreCase(type.toString())) {
                throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + result.getClass().getSimpleName().toLowerCase());
            }
            lastId = Math.max(lastId, id);
        }
        return result;
    }

    public void clear() {
        ticking.clear();
        rootScope.variables.clear();
        everything.clear();
        lastId = 0;
        for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
            View child = rootView.getChildAt(i);
            if (child instanceof ImageView) {
                rootView.removeViewAt(i);
            }
        }
    }

    public void save(String name) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(rootDir, name)), "utf-8");
            dump(writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String name) {
        try {
            loading = true;
            File file = new File(rootDir, name);
            if (!file.exists()) {
                throw new RuntimeException("File '" + name + "' does not exist.");
            }
            clear();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));

            StringBuilder pending = new StringBuilder();
            int balance = 0;
            int lineNumber = 0;

            while (true) {
                String line = reader.readLine();
                lineNumber++;
                if (line == null) {
                    break;
                }

                pending.append(line).append('\n');
                balance += getBalance(line);

                if (balance < 0) {
                    throw new RuntimeException("Too many } in line " + lineNumber);
                }

                if (balance == 0) {
                    line = pending.toString();
                    pending.setLength(0);

                    try {
                        Evaluable e = parse(line);
                        if (e != null) {
                            e.eval(getRootContext());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            environmentListener.setTitle(name);

            if (balance != 0) {
                throw new RuntimeException("Unbalanced input!");
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            loading = false;
        }
    }

    public void pause(boolean paused) {
        if (paused != this.paused) {
            this.paused = paused;
            environmentListener.paused(paused);
        }
    }

    public Type resolveType(String name) {
        if (name.equals("sprite")) {
            return Type.forJavaClass(Sprite.class);
        }
        return null;
    }

    public Context getRootContext() {
        if (rootScope.nextIndex > 0) {
            if (rootContext.variables == null) {
                rootContext.variables = new Object[rootScope.nextIndex];
            } else if (rootScope.nextIndex > rootContext.variables.length) {
                Object[] newVars = new Object[rootScope.nextIndex];
                System.arraycopy(rootContext.variables, 0, newVars, 0, rootContext.variables.length);
                rootContext.variables = newVars;
            }
        }
        return rootContext;
    }


    public interface EnvironmentListener {
        void paused(boolean paused);
        void setTitle(String name);
    }
}
