package org.kobjects.codechat.lang;

import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.api.Emoji;
import org.kobjects.codechat.expr.Assignment;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.RelationalOperator;
import org.kobjects.codechat.expr.UnaryOperator;
import org.kobjects.codechat.expr.UnresolvedInvocation;
import org.kobjects.codechat.expr.BinaryOperator;
import org.kobjects.codechat.expr.InstanceReference;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.CountStatement;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.statement.OnStatement;
import org.kobjects.codechat.statement.Statement;
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

    private static String EMOJI_REGEX =
            "[\\u20a0-\\u32ff\\x{1f000}-\\x{1ffff}][\\x{1F1E6}-\\x{1f1ff}\\x{1f3fe}-\\x{1f3fe}]?";
    private static Pattern EMOJI_PATTERN = Pattern.compile("\\G(" + EMOJI_REGEX + ")");

    private static Pattern IDENTIFIER_PATTERN = Pattern.compile(
            "\\G\\s*(([\\p{Alpha}_$][\\p{Alpha}_$\\d]*(#\\d+)?)|(" + EMOJI_REGEX + "))");

    private final Environment environment;
    private final ExpressionParser<Expression> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Block parseBlock(ExpressionParser.Tokenizer tokenizer, Scope scope, String end) {
        ArrayList<Statement> statements = new ArrayList<>();
        while(true) {
            while(tokenizer.tryConsume(";")) {
                //
            }
            if (tokenizer.tryConsume(end)) {
                break;
            }
            statements.add(parseStatement(tokenizer, scope));
        }
        return new Block(statements.toArray(new Statement[statements.size()]));
    }

    CountStatement parseCount(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        String varName = tokenizer.consumeIdentifier();
        Expression expression = parseExpression(tokenizer, scope);

        if (!expression.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Count expression must be a number.");
        }

        Scope countScope = new Scope(scope);

        Variable counter = countScope.addVariable(varName, Type.NUMBER);

        tokenizer.consume("{");

        Block block = parseBlock(tokenizer, countScope, "}");

        return new CountStatement(counter, expression, block);
    }

    OnStatement parseOn(ExpressionParser.Tokenizer tokenizer, String name) {
        final Expression condition = parseExpression(tokenizer, environment.rootScope);

        boolean needsClose;
        if (tokenizer.currentValue.equals(":")) {
            tokenizer.consume(":");
            needsClose = false;
        } else {
            tokenizer.consume("{");
            needsClose = true;
        }

        final Block exec = parseBlock(tokenizer, environment.rootScope, needsClose ? "}" : "");

        int cut = name.indexOf('#');
        int instanceId;
        if (cut == -1) {
            instanceId = ++environment.lastId;
        } else {
            instanceId = Integer.parseInt(name.substring(cut + 1));
            WeakReference<Instance> reference = environment.everything.get(instanceId);
            Object o = reference == null ? null : reference.get();
            if (o instanceof OnStatement) {
                OnStatement on = (OnStatement) o;
                on.body = exec;
                on.condition = condition;
                return on;
            } else if (o != null) {
                throw new RuntimeException("Object type mismatch with " + o);
            }
        }
        OnStatement result = new OnStatement(environment, instanceId, condition, exec);
        environment.lastId = Math.max(environment.lastId, instanceId);
        environment.everything.put(instanceId, new WeakReference<Instance>(result));
        return result;
    }

    IfStatement parseIf(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        final Expression condition = parseExpression(tokenizer, scope);
        tokenizer.consume("{");
        final Block body = parseBlock(tokenizer, scope, "}");
        return new IfStatement(condition, body);
    }


    Statement parseStatement(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        if (tokenizer.tryConsume("count")) {
            return parseCount(tokenizer, scope);
        }
        if (tokenizer.tryConsume("delete")) {
            return new DeleteStatement(parseExpression(tokenizer, scope));
        }
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#")) {
            String name = tokenizer.consumeIdentifier();
            return parseOn(tokenizer, name);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(tokenizer, scope);
        }
        Expression expression = parseExpression(tokenizer, scope);
        return new ExpressionStatement(expression);
    }

    Expression parseExpression(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        Expression unresolved = expressionParser.parse(tokenizer);

        if (scope == environment.rootScope && unresolved instanceof Assignment) {
            Assignment op = (Assignment) unresolved;
            if (op.left instanceof Identifier) {
                Expression right = op.right.resolve(scope);

                scope.ensureVariable(((Identifier) op.left).name, right.getType());

                return new Assignment(op.left.resolve(scope), right);
            }
        }
        return unresolved.resolve(scope);
    }

    public Statement parse(String line) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(line);
        tokenizer.nextToken();
        Statement statement = parseStatement(tokenizer, environment.rootScope);
        while (tokenizer.tryConsume(";"))
        tokenizer.consume("");
        return statement;
    }

    public ExpressionParser.Tokenizer createTokenizer(String s) {
        return createTokenizer(new StringReader(s));
    }

    public ExpressionParser.Tokenizer createTokenizer(Reader reader) {
        ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
                new Scanner(reader),
                expressionParser.getSymbols(), ":", "{", "}");
        tokenizer.identifierPattern = IDENTIFIER_PATTERN;
        return tokenizer;
    }



    public static class Processor extends ExpressionParser.Processor<Expression> {

        @Override
        public Expression infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression left, Expression right) {
            switch (name) {
                case ".":
                case "'s":
                    return new PropertyAccess(left, right);
                case "=":
                    return new Assignment(left, right);
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "==":
                case "!=":
                case "\u2260":
                case "\u2264":
                case "\u2265":
                    return new RelationalOperator(name, left, right);
                default:
                    return new BinaryOperator(name.charAt(0), left, right);
            }
        }

        @Override
        public Expression implicitOperator(ExpressionParser.Tokenizer tokenizer, boolean strong, Expression left, Expression right) {
            if (left instanceof UnresolvedInvocation && !((UnresolvedInvocation) left).parens) {
                UnresolvedInvocation li = (UnresolvedInvocation) left;
                Expression[] children = new Expression[li.children.length + 1];
                System.arraycopy(li.children, 0, children, 0, li.children.length);
                children[li.children.length] = right;
                return new UnresolvedInvocation(li.name, false, children);
            }
            if (left instanceof Identifier) {
                return new UnresolvedInvocation(((Identifier) left).name, false, right);
            }
            throw new RuntimeException("Method invocations must start with name");
        }

        @Override
        public Expression prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression argument) {
            return new UnaryOperator(name.charAt(0), argument);
        }

        @Override
        public Expression numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(Double.parseDouble(value));
        }

        @Override
        public Expression identifier(ExpressionParser.Tokenizer tokenizer, String name) {

            if (EMOJI_PATTERN.matcher(name).matches()) {
                return new Literal(new Emoji(name));
            }

            if (name.equals("true")) {
                return new Literal(Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new Literal(Boolean.FALSE);
            }
            if (name.indexOf('#') != -1) {
                return new InstanceReference(name);
            }
            return new Identifier(name);
        }

        @Override
        public Expression group(ExpressionParser.Tokenizer tokenizer, String paren, List<Expression> elements) {
            return elements.get(0);
        }

        @Override
        public Expression stringLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(ExpressionParser.unquote(value));
        }

        @Override
        public Expression emoji(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(new Emoji(value));
        }

        @Override
        public Expression call(ExpressionParser.Tokenizer tokenizer, String identifier, String bracket, List<Expression> arguments) {
            return new UnresolvedInvocation(identifier, true, arguments.toArray(new Expression[arguments.size()]));
        }
    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<Expression> createExpressionParser() {
        ExpressionParser<Expression> parser = new ExpressionParser<>(new Processor());
        parser.addCallBrackets("(", ",", ")");
        parser.addGroupBrackets("(", null, ")");

   //     parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_HASH, "#");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, "'s");
        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_SIGN, "^");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-", "\u221a");

       // parser.setImplicitOperatorPrecedence(true, 2);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");
        parser.setImplicitOperatorPrecedence(false, PRECEDENCE_IMPLICIT);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=", "\u2264", "\u2265");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=", "==", "!=", "\u2260");
        return parser;
    }

}
