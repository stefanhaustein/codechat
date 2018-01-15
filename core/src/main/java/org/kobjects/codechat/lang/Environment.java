package org.kobjects.codechat.lang;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.InstanceLink;
import org.kobjects.codechat.annotation.ExecLink;
import org.kobjects.codechat.annotation.Link;
import org.kobjects.codechat.annotation.VariableLink;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.parser.ParsingEnvironment;
import org.kobjects.codechat.statement.HelpStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.Typed;
import org.kobjects.codechat.type.UserClassType;

public class Environment implements ParsingEnvironment {

    public static final CharSequence ABOUT_TEXT = new AnnotatedStringBuilder()
            .append("CodeChat is an application for 'casual' coding on mobile devices using a 'chat-like' interface.\n\n")
            .append("Type '")
            .append("help", new Link() {
                @Override
                public void execute(Environment environment) {
                    HelpStatement.printGeneralHelp(environment);
                }
            })
            .append("' for help on how to use this app and builtin functionionality.\n\n")
            .append("Copyright (C) 2017 Stefan Haustein.\n\n")
            .append("Emoji icons supplied by EmojiOne.\n")
            .append("Explosion sound by Ryan Snook licensed under CC BY-NC 3.0\n")
            .build();


    private int suspended;
    int lastId;
    private TreeMap<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    private WeakHashMap<Instance,Integer> ids = new WeakHashMap<>();
    public WeakHashMap<Instance,String> constants = new WeakHashMap<>();
    public TreeMap<String,RootVariable> rootVariables = new TreeMap<>();
    public File codeDir;
    public EnvironmentListener environmentListener;
    Parser parser = new Parser(this);
    public boolean autoSave = true;

    public Environment(EnvironmentListener environmentListener, File codeDir) {
        this.environmentListener = environmentListener;
        this.codeDir = codeDir;

        addType(Type.BOOLEAN, Type.NUMBER, Type.STRING);
        addType(OnInstance.ON_CHANGE_TYPE, OnInstance.ON_INTERVAL_TYPE, OnInstance.ON_TYPE);

        addBuiltins();
    }

    public static Type typeOf(Object o) {
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        if (o instanceof Typed) {
            Type result = ((Typed) o).getType();
            if (result == null) {
                throw  new RuntimeException("Typed.getType null for" + o + " class " + o.getClass());
            }
            return result;
        }
        if (o instanceof Boolean) {
            return Type.BOOLEAN;
        }
        if (o instanceof Double) {
            return Type.NUMBER;
        }
        if (o instanceof String) {
            return Type.STRING;
        }
        if (o instanceof Type) {
            return new MetaType((Type) o);
        }
        return Type.ANY;
    }

    public List<RootVariable> getErrors() {
        ArrayList<RootVariable> result = new ArrayList<>();
        for (RootVariable var : rootVariables.values()) {
            if (var.error != null) {
                result.add(var);
            }
        }
        return result;
    }

    private void addBuiltins() {
        for (MathFnType mathFnType : MathFnType.values()) {
            addSystemConstant(mathFnType.name().toLowerCase(), new MathFn(mathFnType), mathFnType.documentation);
        }

        addSystemConstant("PI", Math.PI, null);
        addSystemConstant("\u03c0", Math.PI, null);
        addSystemConstant("TAU", 2 * Math.PI, null);
        addSystemConstant("\u03c4", 2 * Math.PI, null);

        final Type type = Type.ANY;
        addSystemConstant("print", new NativeFunction(null,  type) {
                    @Override
                    protected Object eval(Object[] params) {
                        environmentListener.print(String.valueOf(params[0]), EnvironmentListener.Channel.OUTPUT);
                        return null;
                    }
                }, "Prints the argument.");

        addSystemConstant("about", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        environmentListener.print(ABOUT_TEXT, EnvironmentListener.Channel.HELP);
                        return null;
                    }
                }, "Prints copyright information for this application.");

        addSystemConstant("dir", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                        for (File file: codeDir.listFiles()) {
                            String name = file.getName();
                            if (!name.equals("CodeChat")) {
                                asb.append(name, new ExecLink("load \"" + name + "\""));
                                asb.append("\n");
                            }
                        }
                        environmentListener.print(asb.build(), EnvironmentListener.Channel.OUTPUT);
                        return null;
                    }
                }, "Prints the directory of the application file system.");

        addSystemConstant("errors", new NativeFunction(null) {
                   @Override
                protected Object eval(Object[] params) {
                       List<RootVariable> errors = getErrors();
                if (errors.size() == 0) {
                    environmentListener.print("(no errors)", EnvironmentListener.Channel.OUTPUT);
                } else {
                    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                    for (RootVariable errorVar : errors) {
                       asb.append(errorVar.name, new VariableLink(errorVar));
                       asb.append(": ");
                       Formatting.exceptionToString(asb, errorVar.error);
                       asb.append('\n');
                    }
                    environmentListener.print(asb.build(), EnvironmentListener.Channel.OUTPUT);
                }
                return null;
            }
        }, "Prints all errors.");

        addSystemConstant("atan2", new NativeFunction(Type.NUMBER, Type.NUMBER, Type.NUMBER) {
                    @Override
                    protected Object eval(Object[] params) {
                        return Math.atan2((Double) params[0], (Double) params[1]);
                    }
                }, "Computes the angle in radians of the line through (0,0) and (y, x) relative to the x-axis");

        addSystemConstant("clearAll", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        clearAll();
                        environmentListener.setName("CodeChat");
                        return null;
                    }
                }, "Deletes everything and resets the state to the initial state. Use with care.");

        addSystemConstant("resume", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        resume();
                        return null;
                    }
                }, "Resumes after suspend was called. Has no effect otherwise.");

        addSystemConstant("dump", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        dump(Printable.Flavor.SAVE);
                        return null;
                    }
                }, "Prints the current program and state.");

        addSystemConstant("list", new NativeFunction(null) {
            @Override
            protected Object eval(Object[] params) {
                dump(Printable.Flavor.LIST);
                return null;
            }
        }, "Lists constants, variables, functions, procedures and state.");

        addSystemConstant("load", new NativeFunction(null, Type.STRING) {
                    @Override
                    protected Object eval(Object[] params) {
                        load(String.valueOf(params[0]));
                        return null;
                    }
                }, "Loads the program and state previously saved under the given name.");

        addSystemConstant("suspend", new NativeFunction(null) {
                    @Override
                    protected Object eval(Object[] params) {
                        suspend();
                        return null;
                    }
                }, "Pause all events.");

        addSystemConstant("random", new NativeFunction(Type.NUMBER) {
                    @Override
                    protected Object eval(Object[] params) {
                        return Math.random();
                    }
                }, "Return a pseudorandom number >= 0 and < 1.");

        addSystemConstant("save", new NativeFunction(null, Type.STRING) {
                    @Override
                    protected Object eval(Object[] params) {
                        save((String) params[0]);
                        return null;
                    }
                }, "Saves the current program and state under the given name.");

        addSystemConstant("wait", new NativeFunction(null, Type.NUMBER) {
                    @Override
                    protected Object eval(Object[] params) {
                    try {
                        Thread.sleep((long) (((Double) params[0]) * 1000));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    return null;
                }
            }, "Waits for a the given number of seconds.");
    }


    private void dump(Printable.Flavor mode) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        dump(asb, mode);
        String list = asb.toString();
        while (list.endsWith("\n")) {
            list = list.substring(0, list.length() - 1);
        }
        environmentListener.print(new AnnotatedString(list, asb.getAnnotationList()), EnvironmentListener.Channel.OUTPUT);
    }

    public void addType(Type... types) {
        for (Type type : types) {
            addSystemConstant(type.toString(), type, null);
//            addSystemConstant(type.toString().toLowerCase(), type);
        }
    }

    public void addSystemConstant(String name, Object value, String documentaion) {
        RootVariable var = new RootVariable(this, name, typeOf(value), true);
        var.value = value;
        var.builtin = true;
        var.documentation = documentaion;
        if (rootVariables.containsKey(name)) {
            throw new RuntimeException("Already declared: " + name);
        }

        rootVariables.put(name, var);
    }

    @Override
    public String getConstantName(Instance instance) {
        return constants.get(instance);
    }

    public <T extends Instance> T createInstance(InstanceType<T> type, int id) {
        if (id == -1) {
            return type.createInstance(this);
        }

        synchronized (everything) {
            WeakReference<Instance> ref;
            lastId = Math.max(id, lastId);
            ref = everything.get(id);
            Instance existing = ref == null ? null : ref.get();
            if (existing != null) {
                throw new RuntimeException("instance with id " + id + " exists already: " + existing);
            }
            T instance = type.createInstance(this);
            everything.put(id, new WeakReference<Instance>(instance));
            ids.put(instance, id);
            return instance;
        }
    }


    public void dump(AnnotatedStringBuilder asb, Printable.Flavor mode) {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        SerializationContext serializationContext = new SerializationContext(mode);

        // 0: classes
        // 1: constants
        // 2: variables
        // 3: functions


        for (int i = 0; i < 3; i++) {
            for (RootVariable variable : rootVariables.values()) {
                int desired;
                if (variable.constant) {
                    if (variable.value instanceof UserClassType) {
                        desired = 0;
                    } else if (variable.value instanceof UserFunction) {
                        desired = 3;
                    } else {
                        desired = 1;
                    }
                } else {
                    desired = 2;
                }
                if (desired == i) {
                    variable.serialize(asb, serializationContext);
                }
           }
        }

        synchronized (OnInstance.allOnInterval) {
            for (OnInstance onInterval : OnInstance.allOnInterval) {
                serializationContext.enqueue(onInterval);
            }
        }

        while (true) {
            Instance entity = serializationContext.pollPending();
            if (entity == null) {
                break;
            }
            entity.print(asb, serializationContext.getMode());
        }

    }

    public Statement parse(ParsingContext parsingContext, String code) {
        return parser.parse(parsingContext, code);
    }

    public int getId(Instance instance) {
        synchronized (everything) {
            Integer idObject = ids.get(instance);
            if (idObject != null) {
                return idObject.intValue();
            }
            int id = ++lastId;
            everything.put(id, new WeakReference<Instance>(instance));
            ids.put(instance, id);
            return id;
        }
    }


    public <T extends Instance> T getOrCreateInstance(InstanceType<T> type, int id) {
        synchronized (everything) {
            WeakReference reference;
            reference = id == -1 ? null : everything.get(id);
            T result = reference != null ? (T) reference.get() : null;
            if (result == null) {
                result = createInstance(type, id);
            }
            return result;
        }
    }

    /**
     * The force parameter is used for functions with ids and and "on..." expressions that are overwritten.
     */
    public <T extends Instance> T getInstance(InstanceType<T> type, int id) {
        WeakReference reference;
        synchronized (everything) {
            reference = everything.get(id);
            T result = reference != null ? (T) reference.get() : null;
            if (result == null) {
                throw new RuntimeException("Undefined instance reference: " + type + "#" + id);
            } else {
                if (!typeOf(result).equals(type)) {
                    throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + typeOf(result));
                }
            }
            return result;
        }
    }

    public void clearAll() {
        environmentListener.clearAll();
        TreeMap<String, RootVariable> systemVariables = new TreeMap<>();
        for (RootVariable var : rootVariables.values()) {
            if (var.builtin) {
                systemVariables.put(var.name, var);
            }
        }
        for (OnInstance onInterval : OnInstance.allOnInterval) {
            onInterval.detach();
        }
        rootVariables = systemVariables;
        synchronized (everything) {
            ids.clear();
            everything.clear();
        }
        constants.clear();
        autoSave = true;
        lastId = 0;
    }

    public void save(String fileName) {
        try {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
            dump(asb, Printable.Flavor.SAVE);
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(codeDir, fileName)), "utf-8");
            writer.write(asb.toString());
            writer.close();
            autoSave = true;
            environmentListener.setName(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setProgram(String code) {
        suspend();
        try {
            clearAll();
            autoSave = false;

            ArrayList<Exception> errors = new ArrayList<>();
            ParsingContext parsingContext = new ParsingContext(this, ParsingContext.Mode.LOAD);
            Statement statement = parser.parse(parsingContext, code, errors);
            statement.eval(parsingContext.createEvaluationContext(this));

            if (errors.size() > 0) {
                throw new MetaException("Multiple errors:", errors);
            }

            //            autoSave = true;

        } finally {
           resume();
        }

    }

    public void load(String fileName) {
        try {
            File file = new File(codeDir, fileName);
            if (!file.exists()) {
                throw new RuntimeException("File '" + file.getName() + "' does not exist.");
            }

            byte[] data = new byte[(int) file.length()];
            DataInputStream stream = new DataInputStream(new FileInputStream(file));
            stream.readFully(data);
            stream.close();

            String content = new String(data, "utf-8");

            setProgram(content);

            environmentListener.setName(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void exec(String s) {
        ParsingContext parsingContext = new ParsingContext(this, ParsingContext.Mode.INTERACTIVE);
        Statement e = parse(parsingContext, s);
        if (e != null) {
            EvaluationContext evaluationContext = parsingContext.createEvaluationContext(this);
            e.eval(evaluationContext);
        }
    }


    public synchronized void suspend() {
        if (suspended++ == 0) {
            environmentListener.suspended(true);
        }
    }

    public synchronized void resume() {
        if (--suspended == 0) {
            environmentListener.suspended(false);
        }
    }

    public Type resolveType(String name) {
        RootVariable var = rootVariables.get(name);
        if (var == null) {
            throw new RuntimeException("Unknown type: " + name);
        }
        if (!(var.type instanceof MetaType)) {
            throw new RuntimeException("Not a type: " + name);
        }
        return (Type) var.value;
    }

    @Override
    public RootVariable getRootVariable(String name) {
        return rootVariables.get(name);
    }

    Iterable<Instance> findDependencies(RootVariable variable) {
        HashSet<Instance> result = new HashSet<>();
        synchronized (everything) {
            for (WeakReference<Instance> ref : everything.values()) {
                Instance instance = ref.get();
                if (instance != null) {
                    DependencyCollector localDependencies = new DependencyCollector(this);
                    instance.getDependencies(localDependencies);
                    if (localDependencies.contains(variable)) {
                        result.add(instance);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Called when parsing top-level function and variable declarations.
     */
    public RootVariable declareRootVariable(String name, Type type, boolean constant) {
        RootVariable rootVariable = rootVariables.get(name);
        boolean existing = rootVariable != null;
        if (!existing) {
            rootVariable = new RootVariable(this, name, type, constant);
            rootVariables.put(name, rootVariable);
        }
        rootVariable.type = type;
        rootVariable.constant = constant;

        if (existing) {
            for (Instance dependent : findDependencies(rootVariable)) {
                validate(dependent);
            }
        }
        return rootVariable;
    }

    private void validate(Instance dependent) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        dependent.print(asb, Printable.Flavor.EDIT);
        String serialized = asb.toString();

        try {
            parse(new ParsingContext(this, ParsingContext.Mode.INTERACTIVE), serialized);
        } catch (Exception e) {
            asb = new AnnotatedStringBuilder();

            asb.append("Broken dependency: ");
            asb.append(Formatting.toLiteral(dependent), new InstanceLink((Instance) dependent));
        }
    }

    public InstanceType resolveInstanceType(String name) {
        Type result = resolveType(name);
        if (!(result instanceof InstanceType)) {
            throw new RuntimeException(name + " is not an instance type.");
        }
        return (InstanceType) result;
    }

    @Override
    public void removeVariable(String name) {
        rootVariables.remove(name);
    }

    @Override
    public Environment getEnvironment() {
        return this;
    }

    public boolean isSuspended() {
        return suspended > 0;
    }



    enum MathFnType {
        ABS("Returns the absolute value of the argument, i.e. negative values are multiplied with -1."),
        ACOS("Returns the inverse cosine of the argument"),
        ASIN("Returns the inverse sine of the argument"),
        ATAN("Returns the inverse tangens of the argument"),
        CEIL("Returns the closest integer that is larger or equal to the argument"),
        COS("Returns the cosine of the argument"),
        COSH("Returns the hyperbolic cosine of the argument"),
        EXP("Return e to the power of the argument"),
        FLOOR("Returns the closest integer that is smaller than or equal to the argumetn"),
        LOG("Returns the natural logarithm of the argument"),
        LOG10("Returns the logarithm to the base 10 of the argument"),
        ROUND("Returns the argument rounded to the closest integer"),
        SIGNUM("Returns the sign of the argument, i.e. -1 for negative numbers, 0 for 0 and 1 for positive numbers"),
        SIN("Returns the sine of the argument"),
        SINH("Returns the hyperbolic sine of the argument"),
        TAN("Returns the tangens of the argument"),
        TANH("Returns the hyperbolic tangent of the argument");

        final String documentation;

        MathFnType(String documentation) {
            this.documentation = documentation;
        }
    }

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


    /*
    // Used for dump
    static class DependencyData {
        final Entity dependency;
        String name;
        DependencyCollector dependencies = new DependencyCollector();

        DependencyData(Instance instance, Environment environment) {
            this.dependency = instance;
            this.name = instance.getType() + "#" + instance.getId();
            if (instance instanceof HasDependencies) {
                ((HasDependencies) instance).getDependencies(environment, dependencies);
            }
        }

        DependencyData(RootVariable variable, Environment environment) {
            this.dependency = variable;
            this.name = variable.name;
            if (variable instanceof HasDependencies) {
                ((HasDependencies) variable).getDependencies(environment, dependencies);
            }
        }
    }
    */
}
