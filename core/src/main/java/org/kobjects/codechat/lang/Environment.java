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
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;

public class Environment {

    static final CharSequence ABOUT_TEXT = new AnnotatedStringBuilder()
            .append("CodeChat is an application for 'casual' coding on mobile devices using a 'chat-like' interface.\n\n Type '")
            .append("help", new ExecLink("help"))
            .append("' for help on how to use this app and builtin functionionality.\n\n Copyright (C) 2017 Stefan Haustein.")
            .build();


    static final CharSequence HELP_TEXT = new AnnotatedStringBuilder()
            .append("CodeChat is an application for 'casual' coding on mobile devices using a 'chat-like' interface. ")
            .append("Type 'help <object>' to get help on <object>. Type '")
            .append("about", new ExecLink("about"))
            .append("' for copyright information and contributors. ").build();

    static final Map<String,TextLink> helpMap = new TreeMap<>();
    static void addHelp(String what, String text) {
        helpMap.put(what, new TextLink(text));
    }

    static final LinkedHashMap<String, String[]> HELP_LISTS = new LinkedHashMap<String, String[]>(){{
        put("Mathematical operators", new String[]{"^", "\u221a","°",
                "*", "/", "\u00d7", "\u22C5", "%",
                "+", "-",});
        put("Logical operators", new String[]{"and", "or", "not"});
        put("Relational operators", new String[]{"<", "\u2264", "<=", ">", "≥", ">=",
                "=", "\u2261", "==", "\u2260", "!="});
        put("Other operators", new String[]{"new",
                ".",});
        put("Control structures", new String[] {
                "count", "for", "function", "if", "on", "onchange", "var"});
    }};

    static {
        addHelp("new", "'new creates a new 'new' creates a new object of a given type, e.g. new Sprite");
        addHelp(".", "The dot operator ('.') is used to reference individual members of objects, e.g. mySprite.x");
        addHelp("^", "The power operator ('^') calculates the first operand to the power of the second operand. Example: 5^3");
        addHelp("\u221a", "The binary root operator ('\u221a') calculates the nth root of the second operand. Example: 3\u221a27\n" +
                "The unary root operator ('\u221a\') calculates the square root of the argument. Exampe: \u221a25");

        addHelp("\u00ac", "The logical not operator ('\u00ac') negates the argument. Exampe: \u00ac true");
        addHelp("not", "'not' is an alternative spelling for the logical not operator ('\00ac\') to simplify input in some cases. It will be replaced automatically");
        addHelp("°", "The degree operator ('°') converts the argument from degree to radians. Example: 180°");

        addHelp("\u00d7", "The multiplication operator ('\u00d7') multiplies the two arguments. Example: 5 \u00d7 4");
        addHelp("*", "The operator '*' is an alternative spelling for the multiplication operator '\u00d7' to simplify input in some cases. It will be replaced automatically.");
        addHelp("/", "The division operator ('/') divides the first argument by the second argument. Example: 10/2");
        addHelp("\u22C5", "The operator '\u22C5' is an alternative spelling for the division operator '/' to simplify input in some cases. It will be replaced automatically");
        addHelp("%", "The percent operator ('%') calculates n percent of the second argument. Example: 50% 10");

        addHelp("+", "The binary plus operator ('+') adds two numbers. Example: 5 + 4");
        addHelp("-", "The binary minus operator subtracts the second argument from the first agrument. Example: 4-4-4\n" +
                "The unary minus operator negates the argument. Example: -5");

        addHelp("<", "The less than operator evaluates to true if the first argument is strictly less than the second argument. Example: 3 < 4");
        addHelp("\u2264", "The less than or equal operator ('\u2264') evaluates to true if the first argument is less than or equal to the second argument. Example: 3 \u2264 3");
        addHelp("<=", "The operator '<=' is an alternative spelling for the less than or equal operator ('\u2264').");

        addHelp(">", "The greater than operator evaluates to true if the first argument is strictly less than the second argument. Example: 4 > 3");
        addHelp("≥", "The 'greater than or equal' operator ('\u2264') evaluates to true if the first argument is greater than or equal to the second argument. Example: 3 ≥ 3");
        addHelp(">=", "The operator '>=' is an alternative spelling for the greater than or equal operator ('≥') to simplify input in some cases. It will be replaced automatically.");

        addHelp("=", "The equals operator '=' returns true if both arguments are equal when used in expressions (typically conditions). Example: 4 = 4\n" +
                "At statement level, this operator is also used for assignments. Example: x = 4\n");
        addHelp("\u2261", "The operator '\u2261' returns true if both arguments are identical.");
        addHelp("==", "The operator '==' is an alternative spelling for the operator '\u2261', provided to simplify input in some cases. It will be replaced automatically");

        addHelp("\u2260", "The inequality operator '\u2261' returns true if both arguments are not equal. Example: 4 \u2261 5");
        addHelp("!=", "The operator '!=' is an alternative spelling for the inequality operator '\u2260', provided to simplify input in some cases. It will be replaced automatically");

        addHelp("\u2227", "The logical and operator '\u2227' yields true if both arguments are true. Example: true \u2227 true");
        addHelp("and", "The operator 'and' is an alternative spelling for the operator '\u2227', provided to simplify input in some cases. It will be replaced automatically.");

        addHelp("\u2228", "The logical or operator '\u2228' yields true if any of the arguments is true. Example: true \u2228 false");
        addHelp("or", "The operator 'or' is an alternative spelling for the operator '\u2228', provided to simplify input in some cases. It will be replaced automatically.");

        addHelp("count", "A 'count' loop counts a variabls from 0 to a given value. Example:\ncount x to 5: print x; end;");
        addHelp("for", "A 'for' loop iterates over a given set of values. Example:\nfor x in List(1, 3, 7): print x; end;");
        addHelp("function", "A 'function' example:\nfunction sqr(n: Number): Number: return n * n; end;");
        addHelp("if", "An 'if' condition gates a code block on a condition. Example:\nif true: print 42; end;");
        addHelp("on", "An 'on' trigger executes a code block on a property condition. Example: on mySprite.x > screen.right: mySprite.dx = -100; end;");
        addHelp("onchange", "An 'onchange' trigger executes a code block whenenver the given property changes.");
        addHelp("var", "A 'var' declaration declares a new (local) variable. For global variables, use an assignment without the 'var' keyword. Example:\nvar x = 4;");
    }


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

    public Environment(EnvironmentListener environmentListener, File codeDir) {
        this.environmentListener = environmentListener;
        this.codeDir = codeDir;

        addType(Type.BOOLEAN, Type.NUMBER, Type.STRING, Type.VOID);

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

        addNativeFunction(new NativeFunction("help", Type.VOID, "Prints a help text describing the argument.", Type.ANY) {
            @Override
            protected Object eval(Object[] params) {
                if (params[0] == null) {
                    AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
                    asb.append(HELP_TEXT);

                    LinkedHashSet<RootVariable>[] builtins = new LinkedHashSet[3];
                    for (int i = 0; i < 3; i++) {
                        builtins[i] = new LinkedHashSet<>();
                    }
                    for (RootVariable var : rootVariables.values()) {
                        if (var.builtin) {
                            if (var.value instanceof Type) {
                                builtins[0].add(var);
                            } else if (var.value instanceof Function) {
                                builtins[1].add(var);
                            } else {
                                builtins[2].add(var);
                            }
                        }
                    }

                    for (int i = 0; i  < 3; i++) {
                        switch (i) {
                            case 0:
                                asb.append("\nBuilt in types: ");
                                break;
                            case 1:
                                asb.append("\nBuilt in functions: ");
                                break;
                            case 2:
                                asb.append("\nBuilt in constants: ");
                                break;
                        }
                        boolean first = true;
                        for (RootVariable var : builtins[i]) {
                            if (first) {
                                first = false;
                            } else {
                                asb.append(", ");
                            }
                            asb.append(var.name, i == 2 ? new TextLink(String.valueOf(var.value)) : new DocumentedLink((Documented) var.value));
                        }
                    }

                    for (Map.Entry<String,String[]> entry : HELP_LISTS.entrySet()) {
                        asb.append("\n").append(entry.getKey()).append(":");

                        for (int i = 0; i < entry.getValue().length; i++) {
                            if (i > 0) {
                                asb.append(", ");
                            }
                            String op = entry.getValue()[i];
                            asb.append(op, helpMap.get(op));
                        }
                    }

                    environmentListener.print(asb);
                } else if (params[0] instanceof Documented) {
                    environmentListener.print(((Documented) params[0]).getDocumentation());
                } else {
                    Type type = Type.of(params[0]);
                    environmentListener.print(params[0] + " is an instance of the type " + type);
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

    public Instance instantiate(Type type, int id) {
        if (id == -1) {
            if (loading) {
                id = -2;
            } else {
                id = ++lastId;
            }
        } else {
            lastId = Math.max(id, lastId);
            if (everything.get(id) != null) {
                throw new RuntimeException("instance with id " + id + " exists already.");
            }
        }
        Instance instance = type.createInstance(this, id);
        if (id == -2) {
            anonymousInstances.add(instance);
        } else {
            everything.put(id, new WeakReference<Instance>(instance));
        }
        return instance;
    }



    protected void addExtraRootEntities(SerializationContext serializationContext) {

    }

    public void dump(AnnotatedStringBuilder asb) {
        System.gc();

        SerializationContext serializationContext = new SerializationContext(this);

        for (RootVariable variable : rootVariables.values()) {
            variable.serialize(asb, serializationContext);
        }

        addExtraRootEntities(serializationContext);

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
        TreeMap<String, RootVariable> systemVariables = new TreeMap<>();
        for (RootVariable var : rootVariables.values()) {
            if (var.builtin) {
                systemVariables.put(var.name, var);
            }
        }
        rootVariables = systemVariables;
        everything.clear();
        constants.clear();
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
        File file = new File(codeDir, fileName);
        if (!file.exists()) {
            throw new RuntimeException("File '" + file.getName() + "' does not exist.");
        }

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), "utf-8"));

            environmentListener.setName(file.getName());
            ArrayList<Exception> parsingErrors = new ArrayList<>();
            clearAll();

            StringBuilder pending = new StringBuilder();
            int lineNumber = 0;

            while (true) {
                lineNumber++;

                String line = reader.readLine();

                if (line == null || (!line.startsWith(" ") && !line.equals("}") && !line.equals("end") && !line.equals("end;"))) {
                    String statement = pending.toString();
                    if (!statement.isEmpty()) {
                        try {
                            exec(statement);
                        } catch (Exception e) {
                            parsingErrors.add(e);
                        }
                        pending.setLength(0);
                    }
                }
                if (line == null) {
                    break;
                }
                pending.append(line).append('\n');
            }
            if (parsingErrors.size() == 1) {
                throw parsingErrors.get(0);
            } else if (parsingErrors.size() > 0) {
                throw new RuntimeException(parsingErrors.toString());
            }
            autoSave = true;
        } catch (Exception e) {
            autoSave = false;
            throw new RuntimeException(e);
        } finally {
            for (Instance instance: anonymousInstances) {
                int id = ++lastId;
                instance.setId(id);
                everything.put(id, new WeakReference<Instance>(instance));
            }
            loading = false;
            anonymousInstances = null;
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
        SerializationContext serializationContext = new SerializationContext(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
        serializationContext.setState(dependent, SerializationContext.SerializationState.UNVISITED);
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

    public void addFunction(String name, Function function) {
        RootVariable var = new RootVariable();
        var.name = name;
        var.constant = true;
        var.type = function.getType();
        var.value = function;
        rootVariables.put(name, var);
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
