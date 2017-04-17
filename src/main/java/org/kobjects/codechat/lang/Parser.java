package org.kobjects.codechat.lang;

import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.api.Emoji;
import org.kobjects.codechat.expr.FunctionCall;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.Implicit;
import org.kobjects.codechat.expr.InfixOperator;
import org.kobjects.codechat.expr.InstanceRef;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.Node;
import org.kobjects.codechat.expr.Property;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.Delete;
import org.kobjects.codechat.statement.On;
import org.kobjects.expressionparser.ExpressionParser;

public class Parser {
    // public static final int PRECEDENCE_HASH = 8;
    public static final int PRECEDENCE_PATH = 7;
    public static final int PRECEDENCE_POWER = 6;
    public static final int PRECEDENCE_SIGN = 5;
    public static final int PRECEDENCE_MULTIPLICATIVE = 4;
    public static final int PRECEDENCE_ADDITIVE = 3;
    public static final int PRECEDENCE_IMPLICIT = 2;
    public static final int PRECEDENCE_RELATIONAL = 1;
    public static final int PRECEDENCE_EQUALITY = 0;

    private final Environment environment;
    ExpressionParser<Node> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Block parseBlock(ExpressionParser.Tokenizer tokenizer, String end) {
        ArrayList<Evaluable> statements = new ArrayList<>();
        while(true) {
            while(tokenizer.tryConsume(";")) {
                //
            }
            if (tokenizer.tryConsume(end)) {
                break;
            }
            statements.add(parseStatement(tokenizer));
        }
        return new Block(statements.toArray(new Evaluable[statements.size()]));
    }

    Evaluable parseStatement(ExpressionParser.Tokenizer tokenizer) {
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#")) {
            String name = tokenizer.consumeIdentifier();
            final Node condition = expressionParser.parse(tokenizer);

            boolean needsClose;
            if (tokenizer.currentValue.equals(":")) {
                tokenizer.consume(":");
                needsClose = false;
            } else {
                tokenizer.consume("{");
                needsClose = true;
            }

            final Block exec = parseBlock(tokenizer, needsClose ? "}" : "");

            int cut = name.indexOf('#');
            int instanceId;
            if (cut == -1) {
                instanceId = ++environment.lastId;
            } else {
                instanceId = Integer.parseInt(name.substring(cut + 1));
                Object o = environment.everything.get(instanceId);
                if (o instanceof On) {
                    On on = (On) o;
                    on.body = exec;
                    on.condition = condition;
                    return on;
                } else if (o != null) {
                    throw new RuntimeException("Object type mismatch with " + o);
                }
            }
            On result = new On(environment, instanceId, condition, exec);
            environment.lastId = Math.max(environment.lastId, instanceId);
            environment.everything.put(instanceId, new WeakReference<Instance>(result));
            return result;

        }
        if (tokenizer.tryConsume("delete")) {
            return new Delete(expressionParser.parse(tokenizer));
        }
        return expressionParser.parse(tokenizer);

    }

    public Evaluable parse(String line) {
        ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
                new Scanner(new StringReader(line)),
                expressionParser.getSymbols(), ":", "{", "}");
        tokenizer.identifierPattern = Parser.Processor.IDENTIFIER_PATTERN;

        tokenizer.nextToken();
        return parseStatement(tokenizer);
    }


    public static class Processor extends ExpressionParser.Processor<Node> {


        static Pattern IDENTIFIER_PATTERN = Pattern.compile(
                "\\G\\s*[\\p{Alpha}_$][\\p{Alpha}_$\\d]*(#\\d+)?");

        @Override
        public Node infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node left, Node right) {
            switch (name) {
                case ".":
                case "'s":
                    return new Property(left, right);
                default:
                    return new InfixOperator(name, left, right);
            }
        }

        @Override
        public Node implicitOperator(ExpressionParser.Tokenizer tokenizer, boolean strong, Node left, Node right) {
            if (left instanceof Implicit) {
                Implicit li = (Implicit) left;
                Node[] children = new Node[li.children.length + 1];
                System.arraycopy(li.children, 0, children, 0, li.children.length);
                children[li.children.length] = right;
                return new Implicit(children);
            }
            return new Implicit(left, right);
        }

        @Override
        public Node prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node argument) {
            return new InfixOperator(name, argument, null);
        }

        @Override
        public Node numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(Double.parseDouble(value));
        }

        @Override
        public Node identifier(ExpressionParser.Tokenizer tokenizer, String name) {
            if (name.equals("true")) {
                return new Literal(Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new Literal(Boolean.FALSE);
            }
            if (name.indexOf('#') != -1) {
                return new InstanceRef(name);
            }
            return new Identifier(name);
        }

        @Override
        public Node group(ExpressionParser.Tokenizer tokenizer, String paren, List<Node> elements) {
            return elements.get(0);
        }

        @Override
        public Node stringLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(ExpressionParser.unquote(value));
        }

        @Override
        public Node emoji(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(new Emoji(value));
        }

        /**
         * Delegates function calls to Math via reflection.
         */
        @Override
        public Node call(ExpressionParser.Tokenizer tokenizer, String identifier, String bracket, List<Node> arguments) {
            return new FunctionCall(identifier, arguments);
        }
    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    static ExpressionParser<Node> createExpressionParser() {
        ExpressionParser<Node> parser = new ExpressionParser<>(new Processor());
        parser.addCallBrackets("(", ",", ")");
        parser.addGroupBrackets("(", null, ")");

   //     parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_HASH, "#");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, "'s");
        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_SIGN, "^");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-");
       // parser.setImplicitOperatorPrecedence(true, 2);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");
        parser.setImplicitOperatorPrecedence(false, PRECEDENCE_IMPLICIT);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=");
        return parser;
    }

}
