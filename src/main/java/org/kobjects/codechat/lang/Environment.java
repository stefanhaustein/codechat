package org.kobjects.codechat.lang;

import android.os.Handler;
import android.support.annotation.RestrictTo;
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
import org.kobjects.codechat.api.Screen;
import org.kobjects.codechat.api.Sprite;
import org.kobjects.codechat.api.Ticking;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.StatementInstance;
import org.kobjects.expressionparser.ExpressionParser;

public class Environment implements Runnable {
    public Builtins builtins = new Builtins(this);
    public double scale;
    public FrameLayout rootView;
    public LinkedHashSet<Ticking> ticking = new LinkedHashSet<>();
    public boolean paused;
    Handler handler = new Handler();
    int lastId;
    Map<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    public Map<String,RootVariable> rootVariables = new TreeMap<>();
    public File codeDir;
    public EnvironmentListener environmentListener;
    Parser parser = new Parser(this);
    public Screen screen = new Screen();
    public boolean autoSave;

    public Environment(EnvironmentListener environmentListener, FrameLayout rootView, File codeDir) {
        this.environmentListener = environmentListener;
        this.rootView = rootView;
        this.codeDir = codeDir;

        System.out.println("ROOT DIR: " + codeDir.getAbsolutePath().toString());

        clearAll();
        handler.postDelayed(this, 100);
    }

    public Instance instantiate(Class type, int id) {
        try {
            if (id == -1) {
                id = ++lastId;
            } else {
                lastId = Math.max(id, lastId);
                if (everything.get(id) != null) {
                    throw new RuntimeException("instance with id " + id + " exists already.");
                }
            }
            Instance instance = (Instance) type.getConstructor(Environment.class, Integer.TYPE).newInstance(this, id);
            everything.put(id, new WeakReference<Instance>(instance));
            if (instance instanceof Ticking) {
                synchronized (ticking) {
                    ticking.add((Ticking) instance);
                }
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


    public void dump(Writer writer) throws IOException {
        System.gc();

        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null && !(instance instanceof StatementInstance) &&
                    !(instance instanceof OnInstance)) {
                writer.write("new ");
                writer.write(instance.toString());
                writer.write("\n");
            }
        }
        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.dump(writer);
            }
        }
        for (RootVariable variable : rootVariables.values()) {
            if (variable.value != null) {
                writer.write(variable.name);
                writer.write(" = ");
                writer.write(toLiteral(variable.value));
                writer.write("\n");
            }
        }
        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null && (instance instanceof StatementInstance || instance instanceof OnInstance)) {
                String s = instance.toString();
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
            return Formatting.quote((String) o);
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


    public Statement parse(String line, ParsingContext parsingContext) {
        return parser.parse(line, parsingContext);
    }


    public Object getInstance(Type type, int id, boolean force) {
        WeakReference reference = everything.get(id);
        Object result = reference != null ? reference.get() : null;
        if (result == null) {
            if (!force) {
                throw new RuntimeException("Undefined instance reference: " + type + "#" + id);
            }
            Class<?> c = type.getJavaClass();
            result = instantiate(c, id);
        } else {
            if (!result.getClass().getSimpleName().equalsIgnoreCase(type.toString())) {
                throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + result.getClass().getSimpleName().toLowerCase());
            }
            lastId = Math.max(lastId, id);
        }
        return result;
    }

    public void clearAll() {
        synchronized (ticking) {
            ticking.clear();
        }
        rootVariables.clear();
        everything.clear();
        lastId = 0;
        for (int i = rootView.getChildCount() - 1; i >= 0; i--) {
            View child = rootView.getChildAt(i);
            if (child instanceof ImageView) {
                rootView.removeViewAt(i);
            }
        }
    }

    public void save(String fileName) {
        try {
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(codeDir, fileName)), "utf-8");
            dump(writer);
            writer.close();
            autoSave = true;
            environmentListener.setName(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String fileName) {
        File file = new File(codeDir, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File '" + file.getName() + "' does not exist.");
        }

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "utf-8"));

            environmentListener.setName(file.getName());
            boolean success = true;
            clearAll();

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
                        ParsingContext parsingContext = new ParsingContext(this);
                        Statement e = parse(line, parsingContext);
                        if (e != null) {
                            EvaluationContext evaluationContext = parsingContext.createEvaluationContext();
                            e.eval(evaluationContext);
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing line: " + line);
                        e.printStackTrace();
                        success = false;
                    }
                }
            }
            if (balance != 0) {
                throw new RuntimeException("Unbalanced input!");
            }
            if (!success) {
                throw new RuntimeException("Parsing error(s)");
            }
            autoSave = true;
        } catch (Exception e) {
            autoSave = false;
            throw new RuntimeException(e);
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
        if (name.equals("on") || name.equals("onchange")) {
            return Type.forJavaClass(OnInstance.class);
        }
        if (name.equals("number")) {
            return Type.NUMBER;
        }
        if (name.equals("boolean")) {
            return Type.BOOLEAN;
        }
        if (name.equals("string")) {
            return Type.STRING;
        }
        return null;
    }

    public void ensureRootVariable(String name, Type type) {
        RootVariable rootVariable = rootVariables.get(name);
        if (rootVariable == null) {
            rootVariable = new RootVariable();
            rootVariable.name = name;
            rootVariable.type = type;
            rootVariables.put(name, rootVariable);
        } else if (!rootVariable.type.isAssignableFrom(type)) {
            throw new RuntimeException("Can't assign type " + type + " to variable " + name + " of type " + rootVariable.type);
        }
    }

    /*
    public EvaluationContext getRootContext() {
        int varCount = rootParsingContext.getVarCount();
        if (varCount > 0) {
            if (rootContext.variables == null) {
                rootContext.variables = new Object[varCount];
            } else if (varCount > rootContext.variables.length) {
                Object[] newVars = new Object[varCount];
                System.arraycopy(rootContext.variables, 0, newVars, 0, rootContext.variables.length);
                rootContext.variables = newVars;
            }
        }
        return rootContext;
    }
    */


    public interface EnvironmentListener {
        void paused(boolean paused);
        void setName(String name);
        void print(String s);
    }
}
