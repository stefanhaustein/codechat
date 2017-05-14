package org.kobjects.codechat.lang;

import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.api.Emoji;
import org.kobjects.codechat.expr.ArrayIndex;
import org.kobjects.codechat.expr.ArrayLiteral;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.statement.Assignment;
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
import org.kobjects.codechat.statement.VarStatement;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.expressionparser.ExpressionParser;

public class Parser {
    // public static final int PRECEDENCE_HASH = 8;
    public static final int PRECEDENCE_PREFIX = 8;
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

    public static int extractId(String s) {
        int cut = s.indexOf('#');
        return cut == -1 ? -1 : Integer.parseInt(s.substring(cut + 1));
    }

    private final Environment environment;
    private final ExpressionParser<Expression> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Block parseBlock(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext, String end) {
        ArrayList<Statement> statements = new ArrayList<>();
        while(true) {
            while(tokenizer.tryConsume(";")) {
                //
            }
            if (tokenizer.tryConsume(end)) {
                break;
            }
            statements.add(parseStatement(tokenizer, parsingContext, false));
        }
        return new Block(statements.toArray(new Statement[statements.size()]));
    }

    CountStatement parseCount(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext) {
        String varName = tokenizer.consumeIdentifier();
        Expression expression = parseExpression(tokenizer, parsingContext);

        if (!expression.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Count expression must be a number.");
        }

        ParsingContext countParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = countParsingContext.addVariable(varName, Type.NUMBER);

        tokenizer.consume("{");

        Block block = parseBlock(tokenizer, countParsingContext, "}");

        return new CountStatement(counter, expression, block);
    }

    Statement parseBody(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext) {
        if (tokenizer.tryConsume(":")) {
            return parseStatement(tokenizer, parsingContext, false);
        }
        if (tokenizer.tryConsume("{")) {
            return parseBlock(tokenizer, parsingContext, "}");
        }
        throw new RuntimeException("':' or '{' expected for body.");
    }

    Object resolveOrCreate(String reference) {
        int cut = reference.indexOf('#');
        int instanceId;
        Type type;
        if (cut == -1) {
            instanceId = -1;
            type = environment.resolveType(reference);
        } else {
            instanceId = Integer.parseInt(reference.substring(cut + 1));
            String typeName = reference.substring(0, cut);
            type = environment.resolveType(typeName);
            if (type == null) {
                throw new RuntimeException("Unknown type: " + typeName);
            }
            WeakReference<Instance> weakReference = environment.everything.get(instanceId);
            Object o = weakReference == null ? null : weakReference.get();
            if (o != null) {
                if (o.getClass() != type.getJavaClass()) {
                    throw new RuntimeException("Object type mismatch with " + o);
                }
                return o;
            }
        }
        return environment.instantiate(type.getJavaClass(), instanceId);
    }


    OnExpression parseOn(boolean onChange, ExpressionParser.Tokenizer tokenizer, int id, ParsingContext parsingContext) {
        ParsingContext closureParsingContext = new ParsingContext(parsingContext, true);
        final Expression expression = parseExpression(tokenizer, closureParsingContext);

        if (onChange) {
            if (!(expression instanceof PropertyAccess)) {
                throw new RuntimeException("Expression is not a property: " + expression);
            }
        } else {
            if (!expression.getType().equals(Type.BOOLEAN)) {
                throw new RuntimeException("Expression must be boolean: " + expression);
            }
        }

        final Statement body = parseBody(tokenizer, closureParsingContext);
        OnExpression result = new OnExpression(onChange, id, expression, body, closureParsingContext.getClosure());
        return result;
    }



    IfStatement parseIf(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext) {
        final Expression condition = parseExpression(tokenizer, parsingContext);
        final Statement body = parseBody(tokenizer, parsingContext);
        return new IfStatement(condition, body);
    }

    VarStatement parseVar(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext) {
        String varName = tokenizer.consumeIdentifier();
        tokenizer.consume("=");
        Expression init = parseExpression(tokenizer, parsingContext);

        LocalVariable variable = parsingContext.addVariable(varName, init.getType());
        return new VarStatement(variable, init);
    }

    Statement parseStatement(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext, boolean interactive) {
        if (tokenizer.tryConsume("count")) {
            return parseCount(tokenizer, parsingContext);
        }
        if (tokenizer.tryConsume("delete")) {
            return new DeleteStatement(parseExpression(tokenizer, parsingContext), parsingContext);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(tokenizer, parsingContext);
        }
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#") ||
                tokenizer.currentValue.equals("onchange") || tokenizer.currentValue.startsWith("onchange#")) {
            String name = tokenizer.consumeIdentifier();
            return new ExpressionStatement(parseOn(name.startsWith("onchange"), tokenizer, extractId(name), parsingContext));
        }
        if (tokenizer.tryConsume("var")) {
            return parseVar(tokenizer, parsingContext);
        }
        if (tokenizer.tryConsume("{")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(tokenizer, blockContext, "}");
        }


        Expression unresolved = expressionParser.parse(tokenizer);

        if (unresolved instanceof RelationalOperator && ((RelationalOperator) unresolved).name == '=') {
            RelationalOperator op = (RelationalOperator) unresolved;
            Expression right = op.right.resolve(parsingContext);
            if (op.left instanceof Identifier && parsingContext.parent == null && interactive) {
                String name = ((Identifier) op.left).name;
                if (parsingContext.resolve(name) == null) {
                    environment.ensureRootVariable(name, right.getType());
                }
            }
            return new Assignment(op.left.resolve(parsingContext), right);
        }

        if (unresolved instanceof Identifier) {
            ArrayList<Expression> params = new ArrayList<>();
            while (!tokenizer.currentValue.equals("") && !tokenizer.currentValue.equals(";")) {
                Expression param = expressionParser.parse(tokenizer);
                params.add(param);
                tokenizer.tryConsume(",");
            }
            if (params.size() > 0) {
                unresolved = new UnresolvedInvocation(((Identifier) unresolved).name, false, params.toArray(new Expression[params.size()]));
            }
        }

        Expression resolved = unresolved.resolve(parsingContext);
        return new ExpressionStatement(resolved);
    }

    Expression parseExpression(ExpressionParser.Tokenizer tokenizer, ParsingContext parsingContext) {
        Expression unresolved = expressionParser.parse(tokenizer);
        return unresolved.resolve(parsingContext);
    }

    public Statement parse(String line, ParsingContext parsingContext) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(line);
        tokenizer.nextToken();
        Statement statement = parseStatement(tokenizer, parsingContext, true);
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



    public class Processor extends ExpressionParser.Processor<Expression> {

        @Override
        public Expression infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression left, Expression right) {
            switch (name) {
                case ".":
                case "'s":
                    return new PropertyAccess(left, right);
                case "\u2261":
                case "==":
                    return new RelationalOperator('\u2261', left, right);
                case "!=":
                case "\u2260":
                    return new RelationalOperator('\u2260', left, right);
                case "<=":
                case "\u2264":
                    return new RelationalOperator('\u2264', left, right);
                case ">=":
                case "\u2265":
                    return new RelationalOperator('\u2264', left, right);
                case "=":
                case "<":
                case ">":
                    return new RelationalOperator(name.charAt(0), left, right);
                default:
                    return new BinaryOperator(name.charAt(0), left, right);
            }
        }
/*
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
*/
        @Override
        public Expression prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression argument) {
            return // name.equals("new") ? new UnresolvedInvocation("new", false, argument) :
                    new UnaryOperator(name.charAt(0), argument);
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
            if (paren.equals("[")) {
                return new ArrayLiteral(elements.toArray(new Expression[elements.size()]));
            }
            return elements.get(0);
        }

        @Override
        public Expression stringLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(ExpressionParser.unquote(value));
        }


        @Override
        public Expression call(ExpressionParser.Tokenizer tokenizer, String identifier, String bracket, List<Expression> arguments) {
            return new UnresolvedInvocation(identifier, true, arguments.toArray(new Expression[arguments.size()]));
        }

        @Override
        public Expression apply(ExpressionParser.Tokenizer tokenizer, Expression to, String bracket, List<Expression> parameterList) {
            if (bracket.equals("[")) {
                return new ArrayIndex(to, parameterList.get(0));
            }
            return super.apply(tokenizer, to, bracket, parameterList);
        }

        @Override
        public Expression primary(ExpressionParser.Tokenizer tokenizer, String name) {
            if (name.equals("new")) {
                Expression expr = expressionParser.parse(tokenizer);
                return new UnresolvedInvocation("new", false, expr);
            }
            return super.primary(tokenizer, name);
        }

    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<Expression> createExpressionParser() {
        ExpressionParser<Expression> parser = new ExpressionParser<>(new Processor());
        parser.addCallBrackets("(", ",", ")");


        parser.addGroupBrackets("(", null, ")");
        parser.addGroupBrackets("[", ",", "]");

        // FIXME: Should be parser.
        // parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_PREFIX, "new");
        parser.addPrimary("new");

        parser.addApplyBrackets(PRECEDENCE_PATH, "[", null, "]");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");

        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_POWER, "^");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-", "\u221a");

       // parser.setImplicitOperatorPrecedence(true, 2);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");
   //     parser.setImplicitOperatorPrecedence(false, PRECEDENCE_IMPLICIT);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=", "\u2264", "\u2265");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=", "==", "!=", "\u2260", "\u2261");

        // FIXME
        // parser.addPrimary("on", "onchange");

        return parser;
    }

}
