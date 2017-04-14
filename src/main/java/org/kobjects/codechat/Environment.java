package org.kobjects.codechat;

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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import org.kobjects.codechat.expr.Node;
import org.kobjects.codechat.statement.On;
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


    double scale;
    FrameLayout rootView;
    public Map<String, Object> variables = new TreeMap<>();
    public List<Ticking> ticking = new ArrayList<>();
    Handler handler = new Handler();
    int lastId;
    Map<Integer,Instance> everything = new TreeMap<>();
    File rootDir;
    boolean loading;

    public Environment(FrameLayout rootView) {
        this.rootView = rootView;
        this.rootDir = rootView.getContext().getFilesDir();
        clear();
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
        for (Ticking t : ticking) {
            if (!(t instanceof Sprite)) {
                writer.write(t.toString());
                writer.write("\n");
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

    Evaluable parse(String line) {
        ExpressionParser<Node> parser = Processor.createParser();
        ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
            new Scanner(new StringReader(line)),
            parser.getSymbols(), ":");

        tokenizer.nextToken();
        if (tokenizer.tryConsume("on")) {
            final Node condition = parser.parse(tokenizer);
            tokenizer.consume(":");
            final Node exec = parser.parse(tokenizer);
            return new On(this, condition, exec);
        }
        if (tokenizer.tryConsume("dump")) {
            return new Evaluable() {
                @Override
                public Object eval(Environment environment) {
                    StringWriter sw = new StringWriter();
                    try {
                        environment.dump(sw);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return sw.toString();
                }

                public String toString() {
                    return "dump";
                }
            };
        }
        return parser.parse(line);
    }


    public Object getInstance(String type, int id) {
        Object result = everything.get(id);
        if (result == null) {
            if (!loading) {
                throw new RuntimeException("Undefined instance reference: " + type + "#" + id);
            }
            Class<?> c = (Class<?>) variables.get(type);
            result = instantiate(c);
        } else if (!result.getClass().getSimpleName().equalsIgnoreCase(type)) {
            throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + result.getClass().getSimpleName().toLowerCase());
        }
        return result;
    }

    public void clear() {
        ticking.clear();
        variables.clear();
        everything.clear();
        lastId = 0;
        variables.put("sprite", Sprite.class);
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
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                try {
                    Evaluable e = parse(line);
                    if (e != null) {
                        e.eval(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            loading = false;
        }
    }
}
