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
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class Environment {

    static final String HELPTEXT = "CodeChat is an application for 'casual' coding on mobile devices using a " +
            "'chat-like' interface. Emojis are supplied by EmojiOne.\n" +
            "Type \"help <object>\" to get help on <object>.";

    static final Map<String,Documented> helpMap = new TreeMap<>();
    static void addHelp(String what, String text) {
        helpMap.put(what, new SimpleDocumented(text));
    }

    static final String[] OPERATOR_LIST = {
            "new",
            ".",
            "^", "\u221a",
            "not", "\u00ac", "°",
            "*", "/", "\u00d7", "\u22C5", "%",
            "+", "-",
            "<", "\u2264", "<=", ">", "≥", ">=",
            "=", "\u2261", "==", "\u2260", "!=",
            "and", "\u2227",
            "or", "\u2228"};

    static final String[] CONTROL_STRUCTURE_LIST = {
            "count", "for", "function", "if", "on", "onchange", "var"
    };

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
        addHelp("function", "A 'function' example:\nfunction sqr(n: number): number: return n * n; end;");
        addHelp("if", "An 'if' condition gates a code block on a condition. Example:\nif true: print 42; end;");
        addHelp("on", "An 'on' trigger executes a code block on a property condition. Example: on mySprite.x > screen.right: mySprite.dx = -100; end;");
        addHelp("onchange", "An 'onchange' trigger executes a code block whenenver the given property changes.");
        addHelp("var", "A 'var' declaration declares a new (local) variable. For global variables, use an assignment without the 'var' keyword. Example:\nvar x = 4;");
    }


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

        addSystemVariable("PI", Math.PI);
        addSystemVariable("\u03c0", Math.PI);
        addSystemVariable("TAU", 2 * Math.PI);
        addSystemVariable("\u03c4", 2 * Math.PI);

        final Type type = Type.ANY;
        addNativeFunction(new NativeFunction("print", Type.VOID, "", type) {
            @Override
            protected Object eval(Object[] params) {
                environmentListener.print(String.valueOf(params[0]), null);
                return null;
            }
        });

        addNativeFunction(new NativeFunction("help", Type.VOID, "Prints a help text describing the argument.", Type.ANY) {
            @Override
            protected Object eval(Object[] params) {
                if (params[0] == null) {
                    ArrayList<Annotation> annotations = new ArrayList<>();
                    StringBuilder sb = new StringBuilder(HELPTEXT);

                    for (int i = 0; i  < 3; i++) {
                        switch (i) {
                            case 0:
                                sb.append("\nBuilt in types: ");
                                break;
                            case 1:
                                sb.append("\nBuilt in functions: ");
                                break;
                            case 2:
                                sb.append("\nBuilt in constants: ");
                                break;
                        }
                        boolean first = true;
                        for (RootVariable var : systemVariables.values()) {
                            if (var.value instanceof Type) {
                                if (i != 0) {
                                    continue;
                                }
                            } else if (var.value instanceof Function) {
                                if (i != 1) {
                                    continue;
                                }
                            } else if (i != 2) {
                                continue;
                            }
                            if (first) {
                                first = false;
                            } else {
                                sb.append(", ");
                            }
                            Annotation.append(sb, var.name, var.value, annotations);
                        }
                    }

                    AnnotatedStringBuilder asb = new AnnotatedStringBuilder(sb, annotations);
                    asb.append("\nOperators: ");

                    for (int i = 0; i < OPERATOR_LIST.length; i++) {
                        if (i > 0) {
                            asb.append(", ");
                        }
                        String op = OPERATOR_LIST[i];
                        asb.append(op, helpMap.get(op));
                    }

                    asb.append("\nControl structures:");

                    for (int i = 0; i < CONTROL_STRUCTURE_LIST.length; i++) {
                        if (i > 0) {
                            asb.append(", ");
                        }
                        String op = CONTROL_STRUCTURE_LIST[i];
                        asb.append(op, helpMap.get(op));
                    }

                    environmentListener.print(sb.toString(), annotations);
                } else if (params[0] instanceof Documented) {
                    ArrayList<Annotation> annotations = new ArrayList<>();
                    String doc = ((Documented) params[0]).getDocumentation(annotations);
                    environmentListener.print(doc, annotations);
                } else {
                    Type type = Type.of(params[0]);
                    environmentListener.print(params[0] + " is an instance of the type " + type, null);
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
                "Saves the current program and state under the given name.") {
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
//            addSystemVariable(type.toString().toLowerCase(), type);
        }
    }

    public void addSystemVariable(String name, Object value) {
        RootVariable var = new RootVariable();
        var.name = name;
        var.type = Type.of(value);
        var.value = value;

        if (rootVariables.containsKey(name)) {
            throw new RuntimeException("Already declared: " + name);
        }

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
        Instance instance = type.createInstance(this, id);
        everything.put(id, new WeakReference<Instance>(instance));
        return instance;
    }

    public void dump(StringBuilder sb, List<Annotation> annotations) throws IOException {
        System.gc();

        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.serialize(sb, Instance.Detail.DECLARATION, annotations);
            }
        }
        for (RootVariable variable : rootVariables.values()) {
            if (variable.value != null && !systemVariables.containsKey(variable.name)) {
                if (!(variable.value instanceof UserFunction) || !((UserFunction) variable.value).isNamed()) {
                    variable.dump(sb);
                }
            }
        }
        for (WeakReference<Instance> reference : everything.values()) {
            Instance instance = reference.get();
            if (instance != null) {
                instance.serialize(sb, Instance.Detail.DEFINITION, annotations);
            }
        }
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
            int lineNumber = 0;

            while (true) {
                lineNumber++;

                String line = reader.readLine();

                if (line == null || (!line.startsWith(" ") && !line.equals("}") && !line.equals("end") && !line.equals("end;"))) {
                    String statement = pending.toString();
                    if (!statement.isEmpty()) {
                        try {
                            ParsingContext parsingContext = new ParsingContext(this);
                            Statement e = parse(parsingContext, statement);
                            if (e != null) {
                                EvaluationContext evaluationContext = parsingContext.createEvaluationContext();
                                e.eval(evaluationContext);
                            }
                        } catch (Exception e) {
                            System.err.println("Error parsing line: " + statement);
                            e.printStackTrace();
                            success = false;
                        }
                        pending.setLength(0);
                    }
                }
                if (line == null) {
                    break;
                }
                pending.append(line).append('\n');
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

    public void addNativeFunction(NativeFunction function) {
        addSystemVariable(function.name, function);
    }

    public void addFunction(String name, Function function) {
        RootVariable var = new RootVariable();
        var.name = name;
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
}
