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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import org.kobjects.codechat.annotation.AnnotatedString;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.annotation.ExecLink;
import org.kobjects.codechat.annotation.TextLink;
import org.kobjects.codechat.parser.Parser;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;

public class Environment {

    static final CharSequence ABOUT_TEXT = new AnnotatedStringBuilder()
            .append("CodeChat is an application for 'casual' coding on mobile devices using a 'chat-like' interface.\n\n Type '")
            .append("help", new ExecLink("help"))
            .append("' for help on how to use this app and builtin functionionality.\n\n Copyright (C) 2017 Stefan Haustein.\n\n")
            .append("Explosion sound by Ryan Snook licensed under CC BY-NC 3.0\n")
            .build();


    public boolean paused;
    int lastId;
    Map<Integer,WeakReference<Instance>> everything = new TreeMap<>();
    public Map<Instance,String> constants = new WeakHashMap<>();
    public TreeMap<String,RootVariable> rootVariables = new TreeMap<>();
    public File codeDir;
    public EnvironmentListener environmentListener;
    Parser parser = new Parser(this);
    public boolean autoSave = true;
    boolean loading;
    List<Instance> anonymousInstances;
    LinkedHashMap<Entity,Exception> errors = new LinkedHashMap<>();

    public Environment(EnvironmentListener environmentListener, File codeDir) {
        this.environmentListener = environmentListener;
        this.codeDir = codeDir;

        addType(Type.BOOLEAN, Type.NUMBER, Type.STRING, Type.VOID);
        addType(OnInstance.ON_CHANGE_TYPE, OnInstance.ON_INTERVAL_TYPE, OnInstance.ON_TYPE);

        addBuiltins();
    }

    private void addBuiltins() {
        for (MathFnType mathFnType : MathFnType.values()) {
            addNativeFunction(new MathFn(mathFnType));
        }

        addSystemVariable("PI", Math.PI);
        addSystemVariable("\u03c0", Math.PI);
        addSystemVariable("TAU", 2 * Math.PI);
        addSystemVariable("\u03c4", 2 * Math.PI);

        final Type type = Type.ANY;
        addNativeFunction(new NativeFunction("print", Type.VOID, "", type) {
            @Override
            protected Object eval(Object[] params) {
                environmentListener.print(String.valueOf(params[0]));
                return null;
            }
        });

        addNativeFunction(new NativeFunction("about", Type.VOID, "Prints copyright information for this application.") {
            @Override
            protected Object eval(Object[] params) {
                environmentListener.print(ABOUT_TEXT);
                return null;
            }
        });

        addNativeFunction(new NativeFunction("dir", Type.VOID, "Prints the directory of the application file system.") {
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
                environmentListener.print(asb.build());
                return null;
            }
        });

        addNativeFunction(new NativeFunction("errors", Type.VOID, "Prints all errors.") {
            @Override
            protected Object eval(Object[] params) {
                if (errors.size() == 0) {
                    environmentListener.print("(no errors)");
                } else {
                    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                    MetaException.toString(errors, asb);
                    environmentListener.print(asb.build());
                }
                return null;
            }
        });

        addNativeFunction(new NativeFunction("atan2", Type.NUMBER,
                "Computes the angle in radians of the line through (0,0) and (y, x) relative to the x-axis",
                Type.NUMBER, Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                return Math.atan2((Double) params[0], (Double) params[1]);
            }
        });
        addNativeFunction(new NativeFunction("clearAll", Type.VOID,
                "Deletes everything and resets the state to the initial state. Use with care.") {
            @Override
            protected Object eval(Object[] params) {
                clearAll();
                environmentListener.setName("CodeChat");
                return null;
            }
        });
        addNativeFunction(new NativeFunction("continue", Type.VOID,
                "Resumes after pause was called. Has no effect otherwise.") {
            @Override
            protected Object eval(Object[] params) {
                pause(false);
                return null;
            }
        });
        addNativeFunction(new NativeFunction("list", Type.VOID,
                "Lists the current program and state.") {
            @Override
            protected Object eval(Object[] params) {
                list();
                return null;
            }
        });


        addNativeFunction(new NativeFunction("load", Type.VOID,
                "Loads the program and state previously saved under the given name.", Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                load(String.valueOf(params[0]));
                return null;
            }
        });
        addNativeFunction(new NativeFunction("pause", Type.VOID,  "Pause all events.") {
            @Override
            protected Object eval(Object[] params) {
                pause(true);
                return null;
            }
        });
        addNativeFunction(new NativeFunction("random", Type.NUMBER,
                "Return a pseudorandom number >= 0 and < 1.") {
            @Override
            protected Object eval(Object[] params) {
                return Math.random();
            }
        });
        addNativeFunction(new NativeFunction("save", Type.VOID,
                "Saves the current program and state under the given name.", Type.STRING) {
            @Override
            protected Object eval(Object[] params) {
                save((String) params[0]);
                return null;
            }
        });
        addNativeFunction(new NativeFunction("wait", Type.VOID,
                "Waits for a the given number of seconds.", Type.NUMBER) {
            @Override
            protected Object eval(Object[] params) {
                try {
                    Thread.sleep((long) (((Double) params[0]) * 1000));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }


    private void list() {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        dump(asb);
        String list = asb.toString();
        while (list.endsWith("\n")) {
            list = list.substring(0, list.length() - 1);
        }
        environmentListener.print(new AnnotatedString(list, asb.getAnnotationList()));
    }

    public void addType(Type... types) {
        for (Type type : types) {
            addSystemVariable(type.toString(), type);
//            addSystemVariable(type.toString().toLowerCase(), type);
        }
    }

    public void addSystemVariable(String name, Object value) {
        RootVariable var = new RootVariable();
        var.name = name;
        var.type = Type.of(value);
        var.value = value;
        var.builtin = true;

        if (rootVariables.containsKey(name)) {
            throw new RuntimeException("Already declared: " + name);
        }

        rootVariables.put(name, var);
    }

    public <T extends Instance> T instantiate(InstanceType<T> type, int id) {
        if (id == -1) {
            if (loading) {
                id = -2;
            } else {
                id = ++lastId;
            }
        } else {
            lastId = Math.max(id, lastId);
            WeakReference<Instance> ref = everything.get(id);
            Instance existing = ref == null ? null : ref.get();
            if (existing != null) {
                 if (!(existing instanceof Entity) || (existing).getUnparsed() == null) {
                     throw new RuntimeException("instance with id " + id + " exists already: " + existing);
                 }
                return (T) existing;
            }
        }
        T instance = type.createInstance(this, id);
        if (id == -2) {
            anonymousInstances.add(instance);
        } else {
            everything.put(id, new WeakReference<Instance>(instance));
        }
        return instance;
    }


    public void dump(AnnotatedStringBuilder asb) {
        System.gc();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {}

        SerializationContext serializationContext = new SerializationContext(this, SerializationContext.Mode.SAVE);

        for (RootVariable variable : rootVariables.values()) {
            serializationContext.enqueue(variable);
        }

        synchronized (OnInstance.allOnInterval) {
            for (OnInstance onInterval : OnInstance.allOnInterval) {
                serializationContext.enqueue(onInterval);
            }
        }

        while (true) {
            Entity entity = serializationContext.pollPending();
            if (entity == null) {
                break;
            }
            entity.serialize(asb, serializationContext);
        }

    }

    public Statement parse(ParsingContext parsingContext, String line) {
        return parser.parse(parsingContext, line);
    }

    public synchronized int createId() {
        return ++lastId;
    }

    public <T extends Instance> T getInstance(InstanceType<T> type, int id, boolean force) {
        WeakReference reference = everything.get(id);
        T result = reference != null ? (T) reference.get() : null;
        if (result == null) {
            if (!force) {
                throw new RuntimeException("Undefined instance reference: " + type + "#" + id);
            }
            result = instantiate(type, id);
        } else {
            if (!Type.of(result).equals(type)) {
                throw new RuntimeException("Class type mismatch; expected " + type + " for id " + id + "; got: " + Type.of(result));
            }
            synchronized (this) {
                lastId = Math.max(lastId, id);
            }
        }
        return result;
    }

    public void clearAll() {
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
        everything.clear();
        constants.clear();
        errors.clear();
        autoSave = true;
        lastId = 0;
    }

    public void save(String fileName) {
        try {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder(new StringBuilder(), null);
            dump(asb);
            Writer writer = new OutputStreamWriter(new FileOutputStream(new File(codeDir, fileName)), "utf-8");
            writer.write(asb.toString());
            writer.close();
            autoSave = true;
            environmentListener.setName(fileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void load(String fileName) {
        anonymousInstances = new ArrayList<>();
        loading = true;

        boolean pausedBefore = paused;
        paused = true;

        File file = new File(codeDir, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File '" + file.getName() + "' does not exist.");
        }

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "utf-8"));

            clearAll();

            ArrayList<RuntimeException> parsingErrors = new ArrayList<>();
            ArrayList<Entity> unparsedEntities = new ArrayList<>();
            int lineNumber = 0;
            boolean commentsOnly = true;

            String line = reader.readLine();
            while (line != null) {
                StringBuilder sb = new StringBuilder();
                while (line != null && (line.startsWith("#") || line.trim().isEmpty())) {
                    sb.append(line).append('\n');
                    line = reader.readLine();
                }
                if (line != null) {
                    sb.append(line).append('\n');
                    line = reader.readLine();
                    while (line != null && (line.startsWith(" ") || line.equals("end") || line.equals("end;"))) {
                        sb.append(line).append('\n');
                        line = reader.readLine();
                    }
                    String statement = sb.toString();
                    if (!statement.isEmpty()) {
                        try {
                            Entity entity = parser.parseStub(statement);
                            if (entity.getUnparsed() != null) {
                                unparsedEntities.add(entity);
                            }
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                            parsingErrors.add(e);
                        }
                        sb.setLength(0);
                    }
                }
            }

            System.err.println("root variables: " + rootVariables.toString().replace(",", "\n  "));

            for (Entity entity : unparsedEntities) {
                try {
                    exec(entity.getUnparsed());
                    entity.setUnparsed(null);
                } catch (RuntimeException e) {
                    e.printStackTrace();
                    errors.put(entity, e);
                }
            }

            autoSave = parsingErrors.size() == 0 && errors.size() == 0;
            if (parsingErrors.size() == 1 && errors.size() == 0) {
                throw parsingErrors.get(0);
            } else if (parsingErrors.size() > 0 || errors.size() > 0) {
                throw new MetaException("Multiple errors loading '" + fileName + "'.", parsingErrors, errors);
            }
        } catch (IOException e) {
            e.printStackTrace();
            autoSave = false;
            throw new RuntimeException(e);
        } finally {
            environmentListener.setName(file.getName());
            for (Instance instance : anonymousInstances) {
                int id = ++lastId;
                instance.setId(id);
                everything.put(id, new WeakReference<Instance>(instance));
            }
            loading = false;
            anonymousInstances = null;
            paused = pausedBefore;
        }
    }

    public void exec(String s) {
        ParsingContext parsingContext = new ParsingContext(this);
        Statement e = parse(parsingContext, s);
        if (e != null) {
            EvaluationContext evaluationContext = parsingContext.createEvaluationContext();
            e.eval(evaluationContext);
        }
    }


    public void pause(boolean paused) {
        if (paused != this.paused) {
            this.paused = paused;
            environmentListener.paused(paused);
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

    Iterable<Entity> findDependencies(Entity entity) {
        HashSet<Entity> result = new HashSet<>();
        for (WeakReference<Instance> ref: everything.values()) {
            Instance instance = ref.get();
            if (instance != null) {
                DependencyCollector localDependencies = new DependencyCollector(this);
                instance.getDependencies(localDependencies);
                if (localDependencies.contains(entity)) {
                    result.add(instance);
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
            rootVariable = new RootVariable();
            rootVariable.name = name;
            rootVariables.put(name, rootVariable);
        }
        rootVariable.type = type;
        rootVariable.constant = constant;

        if (existing) {
            for (Entity dependent : findDependencies(rootVariable)) {
                validate(dependent);
            }
        }
        return rootVariable;
    }

    private void validate(Entity dependent) {
        SerializationContext serializationContext = new SerializationContext(this, SerializationContext.Mode.EDIT);
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        dependent.serialize(asb, serializationContext);
        String serialized = asb.toString();

        try {
            parse(new ParsingContext(this), serialized);
        } catch (Exception e) {
            asb = new AnnotatedStringBuilder();

            asb.append("Broken dependency: ");
            if (dependent instanceof Instance) {
                asb.append(Formatting.toLiteral(dependent), new EntityLink((Instance) dependent));
            } else {
                asb.append(Formatting.toLiteral(dependent));
            }
        }
    }

    public void addNativeFunction(NativeFunction function) {
        addSystemVariable(function.name, function);
    }

    public InstanceType resolveInstanceType(String name) {
        Type result = resolveType(name);
        if (!(result instanceof InstanceType)) {
            throw new RuntimeException(name + " is not an instance type.");
        }
        return (InstanceType) result;
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
            super(type.name().toLowerCase(), Type.NUMBER, type.documentation, Type.NUMBER);
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
