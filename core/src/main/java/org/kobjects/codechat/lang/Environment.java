package org.kobjects.codechat.lang;

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
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.StatementInstance;
import org.kobjects.expressionparser.ExpressionParser;

public class Environment {

    public ArrayList<Object> builtins = new ArrayList<>();

    public boolean paused;
    int lastId;
    Map<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    public Map<String,RootVariable> rootVariables = new TreeMap<>();
    public File codeDir;
    public EnvironmentListener environmentListener;
    Parser parser = new Parser(this);
    public boolean autoSave;
    /** Copied to rootVariables on clearAll */
    public Map<String,RootVariable> systemVariables = new TreeMap<>();

    public Environment(EnvironmentListener environmentListener, File codeDir) {
        this.environmentListener = environmentListener;
        this.codeDir = codeDir;
        this.builtins.add(new Builtins(this));
        this.builtins.add(Math.class);

        addType(Type.BOOLEAN, Type.NUMBER, Type.STRING, Type.VOID);
    }

    public void addType(Type... types) {
        for (Type type : types) {
            addSystemVariable(type.toString(), type);
        }
    }

    public void addSystemVariable(String name, Object value) {
        RootVariable var = new RootVariable();
        var.name = name;
        var.type = Type.forJavaType(value.getClass());
        var.value = value;

        systemVariables.put(name, var);
        rootVariables.put(name, var);
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
            if (variable.value != null && !systemVariables.containsKey(variable.name)) {
                if (variable.value instanceof Function)  {
                    Function function = (Function) variable.value;
                    if (function.definition.name != null) {
                        StringBuilder sb = new StringBuilder();
                        function.definition.serializeSignature(sb);
                        sb.append(";\n");
                        writer.write(sb.toString());
                    }
                } else {
                    writer.write(variable.name);
                    writer.write(" = ");
                    writer.write(toLiteral(variable.value));
                    writer.write("\n");
                }
            }
        }

        for (RootVariable variable : rootVariables.values()) {
            if (!systemVariables.containsKey(variable.name) && variable.value instanceof Function) {
                if (((Function) variable.value).definition.name != null) {
                    writer.write(toLiteral(variable.value));
                } else {
                    writer.write(variable.name);
                    writer.write(" = ");
                    writer.write(toLiteral(variable.value));
                    writer.write("\n");
                }
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


    public Statement parse(ParsingContext parsingContext, String line) {
        return parser.parse(parsingContext, line);
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
        rootVariables.clear();
        rootVariables.putAll(systemVariables);
        everything.clear();
        lastId = 0;
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
                        Statement e = parse(parsingContext, line);
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
        if (name.equals("on") || name.equals("onchange")) {
            return Type.forJavaType(OnInstance.class);
        }
        RootVariable var = rootVariables.get(name);
        if (var.type != Type.META_TYPE) {
            throw new RuntimeException("Not a type: " + name);
        }
        return (Type) var.value;
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

    public interface EnvironmentListener {
        void paused(boolean paused);
        void setName(String name);
        void print(String s);
    }
}
