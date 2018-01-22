package org.kobjects.codechat.statement;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EditTextLink;
import org.kobjects.codechat.annotation.HelpLink;
import org.kobjects.codechat.annotation.TextLink;
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class HelpStatement extends AbstractStatement {

    static final Map<String,CharSequence> helpMap = new TreeMap<>();
    static void addHelp(String what, CharSequence text) {
        if (text instanceof String) {
            String original = (String) text;
            int start = 0;
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            while (true) {
                int pos0 = original.indexOf('`', start);
                if (pos0 == -1) {
                    break;
                }
                asb.append(original.substring(start, pos0));
                int pos1 = original.indexOf('`', pos0 + 1);
                if (pos1 == -1) {
                    throw new RuntimeException("Unterminated quote in " + original);
                }
                String example = original.substring(pos0 + 1, pos1);
                asb.append(example, new EditTextLink(example));
                start = pos1 + 1;
            }
            asb.append(original.substring(start));
            text = asb.build();
        }

        helpMap.put(what, text);
    }

    static final String[] TOPICS = {"commands", "constants", "control", "functions", "operators", "types"};

    static final LinkedHashMap<String, String[]> HELP_LISTS = new LinkedHashMap<String, String[]>(){{
        put("operators", new String[]{".", "^", "\u221a", "°",
                "*", "/", "\u00d7", "\u22C5", "%",
                "+", "-", "<", "\u2264", "<=", ">", "≥", ">=", "=", "\u2261", "==", "\u2260", "!=",
            "and", "new", "not","or", });
        put("control", new String[] {
                "count", "for", "func", "if", "let", "on", "onchange", "oninterval", "proc", "var"});
    }};

    static {
        addHelp("new", "'new creates a new 'new' creates a new object of a given type, e.g. `new Sprite`.");
        addHelp(".", "The dot operator ('.') is used to reference individual members of objects, e.g. `screen.width`.");
        addHelp("^", "The power operator ('^') calculates the first operand to the power of the second operand. Example: `5^3`");
        addHelp("\u221a", "The binary root operator ('\u221a') calculates the nth root of the second operand. Example: `3\u221a27`. " +
                "The unary root operator ('\u221a\') calculates the square root of the argument. Exampe: `\u221a25`");

        addHelp("\u00ac", "The logical not operator ('\u00ac') negates the argument. Exampe: `\u00ac true`");
        addHelp("not", "'not' is an alternative spelling for the logical not operator ('\00ac\') to simplify input in some cases. It will be replaced automatically");
        addHelp("°", "The degree operator ('°') converts the argument from degree to radians. Example: `180°`");

        addHelp("\u00d7", "The multiplication operator ('\u00d7') multiplies the two arguments. Example: `5 \u00d7 4`");
        addHelp("*", "The operator '*' is an alternative spelling for the multiplication operator '\u00d7' to simplify input in some cases. It will be replaced automatically.");
        addHelp("/", "The division operator ('/') divides the first argument by the second argument. Example: `10/2`");
        addHelp("\u22C5", "The operator '\u22C5' is an alternative spelling for the division operator '/' to simplify input in some cases. It will be replaced automatically");
        addHelp("%", "The percent operator ('%') calculates n percent of the second argument. Example: `50% 10`");

        addHelp("+", "The binary plus operator ('+') adds two numbers. Example: `5 + 4`");
        addHelp("-", "The binary minus operator subtracts the second argument from the first argument. Example: `4-4-4`. " +
                "The unary minus operator negates the argument. Example: `-5`");

        addHelp("<", "The less than operator evaluates to true if the first argument is strictly less than the second argument. Example: `3 < 4`");
        addHelp("\u2264", "The less than or equal operator ('\u2264') evaluates to true if the first argument is less than or equal to the second argument. Example: `3 \u2264 3`");
        addHelp("<=", "The operator '<=' is an alternative spelling for the less than or equal operator ('\u2264').");

        addHelp(">", "The greater than operator evaluates to true if the first argument is strictly less than the second argument. Example: `4 > 3`");
        addHelp("≥", "The 'greater than or equal' operator ('\u2264') evaluates to true if the first argument is greater than or equal to the second argument. Example: `3 ≥ 3`");
        addHelp(">=", "The operator '>=' is an alternative spelling for the greater than or equal operator ('≥') to simplify input in some cases. It will be replaced automatically.");

        addHelp("=", "The equals operator '=' returns true if both arguments are equal when used in expressions (typically conditions). Example: `4 = 4`\n" +
                "At statement level, this operator is also used for assignments. Example: `x = 4`\n");
        addHelp("\u2261", "The operator '\u2261' returns true if both arguments are identical.");
        addHelp("==", "The operator '==' is an alternative spelling for the operator '\u2261', provided to simplify input in some cases. It will be replaced automatically");

        addHelp("\u2260", "The inequality operator '\u2261' returns true if both arguments are not equal. Example: `4 \u2261 5`");
        addHelp("!=", "The operator '!=' is an alternative spelling for the inequality operator '\u2260', provided to simplify input in some cases. It will be replaced automatically");

        addHelp("\u2227", "The logical and operator '\u2227' yields true if both arguments are true. Example: `true \u2227 true`");
        addHelp("and", "The operator 'and' is an alternative spelling for the operator '\u2227', provided to simplify input in some cases. It will be replaced automatically.");

        addHelp("\u2228", "The logical or operator '\u2228' yields true if any of the arguments is true. Example: `true \u2228 false`");
        addHelp("or", "The operator 'or' is an alternative spelling for the operator '\u2228', provided to simplify input in some cases. It will be replaced automatically.");

        addHelp("count", "A 'count' loop counts a variabls from 0 to a given value. Example:\n`" +
            "count x to 5:\n" +
            "  print x\n" +
            "end;`");
        addHelp("for", "A 'for' loop iterates over a given set of values.\nExample:\n`" +
            "for x in List(1, 3, 7):\n" +
            "  print x\n" +
            "end`");
        addHelp("func", "A function declaration.\nExample:\n`" +
            "func sqr(n: Number): Number:\n" +
            "  return n * n\n" +
            "end`");
        addHelp("proc", "A procedure declaration.\nExample:\n`" +
            "proc answer():\n"+
            "  print 42\n" +
            "end`");
        addHelp("if", "An 'if' condition gates a code block on a condition.\nExample:\n`" +
            "if random() > 0.5:\n" +
            "  print \"Ok\"\n" +
            "else:\n" +
            "  print \"Computer says no\"\n"+
            "end`");
        addHelp("let", "A 'let' declaration declares a new constant.\nExample:\n`let answer = 42\nprint answer`");
        addHelp("on", "An 'on' trigger executes a code block on a property condition. Example: on mySprite.x > screen.right: mySprite.dx = -100; end;");
        addHelp("onchange", "An 'onchange' trigger executes a code block whenever the given property changes.");
        addHelp("oninterval", "An 'oninterval' trigger executes a code block repeatedly at the given interval in seconds.\n" + "Example:\n`" +
            "oninterval 1:\n" +
            "  print \"tick\"\n" +
            "end`");
        addHelp("variable", "A 'variable' declaration declares a new variable.\nExample:\n`var x = 4\nx += 1\nprint x`");
    }

    static boolean initialized;

    public static void init(Environment environment) {
        if (initialized) {
            return;
        }

        TreeMap<String,String> index = new TreeMap();
        LinkedHashSet<RootVariable>[] builtins = new LinkedHashSet[4];
        for (int i = 0; i < 4; i++) {
            builtins[i] = new LinkedHashSet<>();
        }
        for (RootVariable var : environment.rootVariables.values()) {
            if (var.builtin) {
                if (var.value instanceof Type) {
                    builtins[3].add(var);
                    index.put(var.name, " (type)");
                } else if (var.value instanceof Function) {
                    if (((FunctionType) var.type).returnType == null) {
                        builtins[0].add(var);
                        index.put(var.name, " (command)");
                    } else {
                        builtins[1].add(var);
                        index.put(var.name, " (function)");
                    }
                } else {
                    builtins[2].add(var);
                    index.put(var.name, " (constant)");
                }
            }
        }

        for (int i = 0; i  < 4; i++) {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            String keyword;
            switch (i) {
                case 0:
                    keyword = "commands";
                    break;
                case 1:
                    keyword = "functions";
                    break;
                case 2:
                    keyword = "constants";
                    break;
                case 3:
                    keyword = "types";
                    break;
                default:
                    keyword = "ERROR";
            }
            asb.append("Builtin " + keyword + "\n\n", new Title());
            for (RootVariable var : builtins[i]) {
                asb.append("- ");
                asb.append(var.name, new DocumentedLink(var));
                asb.append("\n");
            }
            addHelp(keyword, asb.build());
        }

        for (Map.Entry<String,String[]> entry : HELP_LISTS.entrySet()) {
            AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
            String topic = entry.getKey();
            String title;
            if (topic.equals("operators")) {
                title = "Builtin Operators";
            } else if (topic.equals("control")) {
                title = "Control Structures";
            } else {
                title = topic;
            }
            asb.append(title + "\n\n", new Title());
            for (int i = 0; i < entry.getValue().length; i++) {
                String op = entry.getValue()[i];
                asb.append(" - ").append(op, new TextLink(helpMap.get(op))).append("\n");
                index.put(op, " (" + topic + ")");
            }
            addHelp(topic, asb.build());
        }

        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append("Help Overview\n\n", new Title());
        asb.append("Type '").append("help", new EditTextLink("help ")).append(" <object>' to get help on <object>; ");
        asb.append("help index", new HelpLink("index")).append(" for the full help index.\n\n");

        asb.append("Other help topics:\n\n");

        asb.append(" - ").append("about", new TextLink(Environment.ABOUT_TEXT)).append("\n");
        for (String topic : TOPICS) {
            asb.append(" - ").append(topic, new HelpLink(topic)).append("\n");
        }

        addHelp("overview", asb.build());

        asb = new AnnotatedStringBuilder();
        asb.append("Full Index\n\n", new Title());

        for (Map.Entry<String, String> entry : index.entrySet()) {
            asb.append(" - ").append(entry.getKey(), new HelpLink(entry.getKey())).append(entry.getValue()).append("\n");
        }

        addHelp("index", asb.build());
    }


    String topic;

    public HelpStatement(String topic) {
        this.topic = topic;
    }


    public static void printHelp(Environment environment, String topic) {
        init(environment);

        if (topic == null || topic.isEmpty()) {
            topic = "overview";
        }

        if (helpMap.containsKey(topic)) {
            environment.environmentListener.print(helpMap.get(topic), EnvironmentListener.Channel.HELP);
        } else if (environment.rootVariables.containsKey(topic)) {
            environment.environmentListener.print(
                Formatting.getDocumentation(environment.rootVariables.get(topic)), EnvironmentListener.Channel.HELP);
        } else {
            environment.environmentListener.print("No help available for \"" + topic + "\"", EnvironmentListener.Channel.HELP);
        }
    }

    @Override
    public Object eval(EvaluationContext context) {
        printHelp(context.environment, topic);
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append("help");
        if (topic != null) {
            sb.append(' ').append(topic);
        }
    }

    @Override
    public void getDependencies(DependencyCollector result) {
    }
}
