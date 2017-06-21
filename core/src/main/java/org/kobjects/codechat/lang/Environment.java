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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.kobjects.codechat.expr.FunctionExpression;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Instantiable;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class Environment {
    public boolean paused;
    int lastId;
    Map<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    public TreeMap<String,RootVariable> rootVariables = new TreeMap<>();
    public File codeDir;
    public EnvironmentListener environmentListener;
    Parser parser = new Parser(this);
    public boolean autoSave;
    /** Copied to rootVariables on clearAll */
    public Map<String,RootVariable> systemVariables = new TreeMap<>();


    public Environment(EnvironmentListener environmentListener, File codeDir) {
        this.environmentListener = environmentListener;
        this.codeDir = codeDir;

        addType(Type.BOOLEAN, Type.NUMBER, Type.STRING, Type.VOID);

        addBuiltins();
    }

    private void addBuiltins() {
        for (MathFnType mathFnType : MathFnType.values()) {
            addFunction(mathFnType.name().toLowerCase(), new MathFn(mathFnType));
        }

        addFunction("print", new PrintFunction(Type.STRING));
        addFunction("print", new PrintFunction(Type.BOOLEAN));
        addFunction("print", new PrintFunction(Type.NUMBER));

        addFunction("atan2", new NativeFunction(Type.NUMBER, Type.NUMBER, Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                return Math.atan2((Double) params[0], (Double) params[1]);
            }
        });
        addFunction("clearAll", new NativeFunction(Type.VOID) {
            @Override
            protected Object eval(Object[] params) {
                clearAll();
                environmentListener.setName("CodeChat");
                return null;
            }
        });
        addFunction("continue", new NativeFunction(Type.VOID) {
            @Override
            protected Object eval(Object[] params) {
                pause(false);
                return null;
            }
        });
        addFunction("dump", new NativeFunction(Type.VOID) {
            @Override
            protected Object eval(Object[] params) {
                list();
                return null;
            }
        });
        addFunction("load", new NativeFunction(Type.VOID, Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                load(String.valueOf(params[0]));
                return null;
            }
        });
        addFunction("pause", new NativeFunction(Type.VOID) {
            @Override
            protected Object eval(Object[] params) {
                pause(true);
                return null;
            }
        });
        addFunction("save", new NativeFunction(Type.VOID, Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                save((String) params[0]);
                return null;
            }
        });
        addFunction("random", new NativeFunction(Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                return Math.random();
            }
        });

    }

    private void list() {
        StringBuilder sb = new StringBuilder();
        List<Annotation> annotations = new ArrayList<>();
        try {
            dump(sb, annotations);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String list = sb.toString();
        while (list.endsWith("\n")) {
            list = list.substring(0, list.length() - 1);
        }

        environmentListener.print(list, annotations);
    }

    public void addType(Type... types) {
        for (Type type : types) {
            addSystemVariable(type.toString(), type);
        }
    }

    public void addSystemVariable(String name, Object value) {
        RootVariable var = new RootVariable();
        var.name = name;
        var.type = Type.of(value);
        var.value = value;

        systemVariables.put(name, var);
        rootVariables.put(name, var);
    }

    public Instance instantiate(Type type, int id) {
        if (id == -1) {
            id = ++lastId;
        } else {
            lastId = Math.max(id, lastId);
            if (everything.get(id) != null) {
                throw new RuntimeException("instance with id " + id + " exists already.");
            }
        }
        Instance instance = ((Instantiable<?>) type).createInstance(this, id);
        everything.put(id, new WeakReference<Instance>(instance));
        return instance;
    }

    public void dump(StringBuilder sb, List<Annotation> annotations) throws IOException {
        System.gc();

        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.serializeDeclaration(sb, annotations);
            }
        }
        for (RootVariable variable : rootVariables.values()) {
            if (variable.value != null && !systemVariables.containsKey(variable.name)) {
                if (variable.value instanceof UserFunction ? ((UserFunction) variable.value).isNamed() : true)  {
                    sb.append(variable.dump(false));
                }
            }
        }
        for (RootVariable variable : rootVariables.values()) {
            if (!systemVariables.containsKey(variable.name) && variable.value instanceof UserFunction) {
                sb.append(variable.dump(true));
            }
        }
        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.serializeDefinition(sb, false);
            }
        }
    }


    public static String toLiteral(Object o) {
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
            result = instantiate(type, id);
        } else {
            if (!Type.of(result).equals(type)) {
                throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + Type.of(result));
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
            StringBuilder sb = new StringBuilder();
            dump(sb, null);
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(codeDir, fileName)), "utf-8");
            writer.write(sb.toString());
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
        if (name.equals("on")) {
            return OnInstance.ON_TYPE;
        }
        if (name.equals("onchange")) {
            return OnInstance.ONCHANGE_TYPE;
        }
        RootVariable var = rootVariables.get(name);
        if (!(var.type instanceof MetaType)) {
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

    public void addFunction(String name, Function function) {
        FunctionType type = function.getType();
        String qualifiedName = FunctionExpression.getQualifiedName(name, type.parameterTypes);
        RootVariable var = new RootVariable();
        var.name = qualifiedName;
        var.type = type;
        var.value = function;
        rootVariables.put(qualifiedName, var);
        if (function instanceof NativeFunction) {
            systemVariables.put(qualifiedName, var);
        }


    }


    enum MathFnType {ABS, ACOS, ASIN, ATAN, CEIL, COS, COSH, EXP, FLOOR, LOG, LOG10, ROUND, SIGNUM, SIN, SINH, TAN, TANH};

    static class MathFn extends NativeFunction {
        MathFnType type;
        MathFn(MathFnType type) {
            super(Type.NUMBER, Type.NUMBER);
            this.type = type;
        }

        @Override
        protected Object eval(Object[] params) {
            double arg = (Double) params[0];
            switch (type) {
                case ABS: return Math.abs(arg);
                case ACOS: return Math.acos(arg);
                case ATAN: return Math.atan(arg);
                case ASIN: return Math.asin(arg);
                case CEIL: return Math.ceil(arg);
                case COS: return Math.cos(arg);
                case COSH: return Math.cosh(arg);
                case EXP: return Math.exp(arg);
                case FLOOR: return Math.floor(arg);
                case LOG: return Math.log(arg);
                case LOG10: return Math.log10(arg);
                case ROUND: return Math.round(arg);
                case SIGNUM: return Math.signum(arg);
                case SIN: return Math.sin(arg);
                case SINH: return Math.sinh(arg);
                case TAN: return Math.tan(arg);
                case TANH: return Math.tanh(arg);
                default:
                    throw new RuntimeException();
            }
        }
    }

    class PrintFunction extends NativeFunction {
        PrintFunction(Type type) {
            super(Type.VOID, type);
        }

        @Override
        protected Object eval(Object[] params) {
            environmentListener.print(String.valueOf(params[0]), null);
            return null;
        }
    }

}
