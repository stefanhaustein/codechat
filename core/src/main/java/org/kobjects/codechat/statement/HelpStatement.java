package org.kobjects.codechat.statement;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.TreeMap;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.EntityLink;
import org.kobjects.codechat.annotation.ExecLink;
import org.kobjects.codechat.annotation.TextLink;
import org.kobjects.codechat.lang.DependencyCollector;
import org.kobjects.codechat.lang.Documented;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EvaluationContext;
import org.kobjects.codechat.lang.Function;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.Type;

public class HelpStatement extends AbstractStatement {

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


    private static void printGeneralHelp(Environment environment) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        asb.append(HELP_TEXT);

        LinkedHashSet<RootVariable>[] builtins = new LinkedHashSet[3];
        for (int i = 0; i < 3; i++) {
            builtins[i] = new LinkedHashSet<>();
        }
        for (RootVariable var : environment.rootVariables.values()) {
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
                asb.append(var.name, new DocumentedLink(var));
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

        environment.environmentListener.print(asb.build());
    }


    String about;

    public HelpStatement(String about) {
        this.about = about;
    }


    @Override
    public Object eval(EvaluationContext context) {
        if (about == null) {
            printGeneralHelp(context.environment);
        } else if (context.environment.rootVariables.containsKey(about)) {
            context.environment.environmentListener.print(context.environment.rootVariables.get(about).getDocumentation());
        } else if (helpMap.containsKey(about)) {
            helpMap.get(about).execute(context.environment);
        } else {
            context.environment.environmentListener.print("No help available for \"" + about + "\"");
        }
        return KEEP_GOING;
    }

    @Override
    public void toString(AnnotatedStringBuilder sb, int indent) {
        sb.append("help");
        if (about != null) {
            sb.append(' ').append(about);
        }
    }

    @Override
    public void getDependencies(DependencyCollector result) {
    }
}
