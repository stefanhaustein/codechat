package org.kobjects.codechat.lang;

import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.expr.ObjectLiteral;
import org.kobjects.codechat.expr.UnresolvedArrayExpression;
import org.kobjects.codechat.expr.FunctionExpression;
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
import org.kobjects.codechat.statement.ForeachStatement;
import org.kobjects.codechat.statement.ReturnStatement;
import org.kobjects.codechat.statement.VarStatement;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;

public class Parser {
    // public static final int PRECEDENCE_HASH = 8;
    public static final int PRECEDENCE_PREFIX = 10;
    public static final int PRECEDENCE_PATH = 8;
    public static final int PRECEDENCE_POWER = 7;
    public static final int PRECEDENCE_SIGN = 6;
    public static final int PRECEDENCE_MULTIPLICATIVE = 5;
    public static final int PRECEDENCE_ADDITIVE = 4;
    public static final int PRECEDENCE_RELATIONAL = 3;
    public static final int PRECEDENCE_EQUALITY = 2;
    public static final int PRECEDENCE_AND = 1;
    public static final int PRECEDENCE_OR = 0;

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
    private final ExpressionParser<Expression, ParsingContext> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Block parseBlock(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String... end) {
        Block block = parseBlockLeaveEnd(parsingContext, tokenizer, end);
        tokenizer.nextToken();
        return block;
    }

    Block parseBlockLeaveEnd(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String... end) {
        ArrayList<Statement> statements = new ArrayList<>();
        outer:
        while(true) {
            while(tokenizer.tryConsume(";")) {
                //
            }
            for (String endToken : end) {
                if (tokenizer.currentValue.equals(endToken)) {
                    break outer;
                }
            }
            statements.add(parseStatement(parsingContext, tokenizer, false));
        }
        return new Block(statements.toArray(new Statement[statements.size()]));
    }

    CountStatement parseCount(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();

        tokenizer.consume("to");

        Expression expression = parseExpression(parsingContext, tokenizer);

        if (!expression.getType().equals(Type.NUMBER)) {
            throw new RuntimeException("Count expression must be a number.");
        }

        ParsingContext countParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = countParsingContext.addVariable(varName, Type.NUMBER);

        Statement body = parseBody(countParsingContext, tokenizer);

        return new CountStatement(counter, expression, body);
    }

    ForeachStatement parseForeach(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();

        tokenizer.consume("in");

        Expression expression = parseExpression(parsingContext, tokenizer);

        if (!(expression.getType() instanceof CollectionType)) {
            throw new RuntimeException("Foreach expression must be a list.");
        }
        Type elementType = ((CollectionType) expression.getType()).elementType;

        ParsingContext foreachParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = foreachParsingContext.addVariable(varName, elementType);

        Statement body = parseBody(foreachParsingContext, tokenizer);
        return new ForeachStatement(counter, expression, body);
    }

    Statement parseBody(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        if (tokenizer.tryConsume(":") || tokenizer.tryConsume("begin")) {   // TODO remove begin
            return parseBlock(parsingContext, tokenizer, "end", "}");
        }
        if (tokenizer.tryConsume("{")) {
            return parseBlock(parsingContext, tokenizer, "}", "end");
        }
        throw new RuntimeException("':' or '{' expected for body.");
    }

    Type parseType(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String typeName = tokenizer.consumeIdentifier();
        Type type = parsingContext.environment.resolveType(typeName);
        return type;
    }

    FunctionExpression parseFunction(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, int id, String name) {
        tokenizer.consume("(");
        ParsingContext bodyContext = new ParsingContext(parsingContext, true);
        ArrayList<String> parameterNames = new ArrayList<String>();
        ArrayList<Type> parameterTypes = new ArrayList<Type>();
        if (!tokenizer.tryConsume(")")) {
            do {
                String paramName = tokenizer.consumeIdentifier();
                tokenizer.consume(":");
                Type type = parseType(parsingContext, tokenizer);
                bodyContext.addVariable(paramName, type);
                parameterNames.add(paramName);
                parameterTypes.add(type);
            } while (tokenizer.tryConsume(","));
            tokenizer.consume(")");
        }

        tokenizer.consume(":");
        Type returnType = parseType(parsingContext, tokenizer);

        FunctionType functionType = new FunctionType(returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));

        Statement body;
        if (tokenizer.tryConsume(";") || tokenizer.tryConsume("")) {
            body = null;
        } else {
            body = parseBody(bodyContext, tokenizer);
        }
        return new FunctionExpression(id, name, functionType, parameterNames.toArray(new String[parameterNames.size()]), bodyContext.getClosure(), body);
    }

    OnExpression parseOn(ParsingContext parsingContext, boolean onChange, ExpressionParser.Tokenizer tokenizer, int id) {
        ParsingContext closureParsingContext = new ParsingContext(parsingContext, true);
        final Expression expression = parseExpression(closureParsingContext, tokenizer);

        if (onChange) {
            if (!(expression instanceof PropertyAccess)) {
                throw new RuntimeException("Expression is not a property: " + expression);
            }
        } else {
            if (!expression.getType().equals(Type.BOOLEAN)) {
                throw new RuntimeException("Expression must be boolean: " + expression);
            }
        }

        final Statement body = parseBody(closureParsingContext, tokenizer);
        OnExpression result = new OnExpression(onChange, id, expression, body, closureParsingContext.getClosure());
        return result;
    }

    IfStatement parseIf(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        Expression condition = parseExpression(parsingContext, tokenizer);

        tokenizer.consume(":");

        Statement ifBody = parseBlockLeaveEnd(parsingContext, tokenizer, "end", "}", "else");

        Statement elseBody = null;
        if (tokenizer.tryConsume("else")) {
            if (tokenizer.tryConsume("{")) {
                elseBody = parseBlock(parsingContext, tokenizer, "}", "end");
            } else {
                tokenizer.tryConsume(":");
                elseBody = parseBlock(parsingContext, tokenizer, "end");
            }
        } else {
            tokenizer.nextToken();
        }
        return new IfStatement(condition, ifBody, elseBody);
    }

    VarStatement parseVar(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();
        tokenizer.consume("=");
        Expression init = parseExpression(parsingContext, tokenizer);

        LocalVariable variable = parsingContext.addVariable(varName, init.getType());
        return new VarStatement(variable, init);
    }



    Statement parseStatement(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, boolean interactive) {
        if (tokenizer.tryConsume("count")) {
            return parseCount(parsingContext, tokenizer);
        }
        if (tokenizer.tryConsume("delete")) {
            return new DeleteStatement(parseExpression(parsingContext, tokenizer), parsingContext);
        }
        if (tokenizer.currentValue.equals("function") || tokenizer.currentValue.startsWith("function#")) {
            int id = extractId(tokenizer.consumeIdentifier());
            String name = tokenizer.consumeIdentifier();
            FunctionExpression functionExpr = parseFunction(parsingContext, tokenizer, id, name);
            return new ExpressionStatement(functionExpr);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(parsingContext, tokenizer);
        }
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#") ||
                tokenizer.currentValue.equals("onchange") || tokenizer.currentValue.startsWith("onchange#")) {
            String name = tokenizer.consumeIdentifier();
            return new ExpressionStatement(parseOn(parsingContext, name.startsWith("onchange"), tokenizer, extractId(name)));
        }
        if (tokenizer.tryConsume("var")) {
            return parseVar(parsingContext, tokenizer);
        }
        if (tokenizer.tryConsume("return")) {
            return new ReturnStatement(parseExpression(parsingContext, tokenizer));
        }
        if (tokenizer.tryConsume("{")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, "}");
        }
        if (tokenizer.tryConsume("begin")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, "end");
        }

        Expression unresolved = expressionParser.parse(parsingContext, tokenizer);

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

        /*
        if (unresolved instanceof UnresolvedInvocation) {
            UnresolvedInvocation invocation = (UnresolvedInvocation) unresolved;
            if (!invocation.parens && invocation.children.length == 1) {
                ArrayList<Expression> newChildren = new ArrayList<>();
                newChildren.add(invocation.children[0]);
                while (tokenizer.tryConsume(",")) {
                    newChildren.add(expressionParser.parse(parsingContext, tokenizer));
                }
                invocation.children = newChildren.toArray(new Expression[newChildren.size()]);
            }
        }*/

        if (unresolved instanceof Identifier) {
            RootVariable var = parsingContext.environment.rootVariables.get(((Identifier) unresolved).name);
            if (var != null && var.functions().iterator().hasNext()) {
                ArrayList<Expression> params = new ArrayList<>();
                while (!tokenizer.currentValue.equals("")
                        && !tokenizer.currentValue.equals(";")
                        && !tokenizer.currentValue.equals("}")
                        && !tokenizer.currentValue.equals("{")
                        && !tokenizer.currentValue.equals("else")
                        && !tokenizer.currentValue.equals("end")) {
                    Expression param = expressionParser.parse(parsingContext, tokenizer);
                    params.add(param);
                    tokenizer.tryConsume(",");
                }
                if (params.size() > 0 || var.value == null) {
                    unresolved = new UnresolvedInvocation(unresolved, false, params.toArray(new Expression[params.size()]));
                }
            }
        }

        Expression resolved = unresolved.resolve(parsingContext);
        return new ExpressionStatement(resolved);
    }

    Expression parseInvocation(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, Expression base) {
        // tokenizer.consume("(");
        if (tokenizer.tryConsume(")")) {
            return new UnresolvedInvocation(base, true);
        }
        Expression first = expressionParser.parse(parsingContext, tokenizer);
        if (first instanceof Identifier && tokenizer.tryConsume(":")) {
            LinkedHashMap<String, Expression> elements = new LinkedHashMap<>();
            Expression firstValue = expressionParser.parse(parsingContext, tokenizer);
            elements.put(((Identifier) first).name, firstValue);
            while (tokenizer.tryConsume(",")) {
                String name = tokenizer.consumeIdentifier();
                tokenizer.consume(":");
                elements.put(name, expressionParser.parse(parsingContext, tokenizer));
            }
            tokenizer.consume(")");
            return new ObjectLiteral(base, elements);
        }
        ArrayList<Expression> args = new ArrayList<>();
        args.add(first);
        while (tokenizer.tryConsume(",")) {
            args.add(expressionParser.parse(parsingContext, tokenizer));
        }
        tokenizer.consume(")");
        return new UnresolvedInvocation(base, true, args.toArray(new Expression[args.size()]));
    }

    Expression parseExpression(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        Expression unresolved = expressionParser.parse(parsingContext, tokenizer);
        return unresolved.resolve(parsingContext);
    }

    public Statement parse(ParsingContext parsingContext, String line) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(line);
        tokenizer.nextToken();
        Statement statement = parseStatement(parsingContext, tokenizer, true);
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
                expressionParser.getSymbols() , ":", "{", "}", "end", "else");
        tokenizer.identifierPattern = IDENTIFIER_PATTERN;
        return tokenizer;
    }



    public class Processor extends ExpressionParser.Processor<Expression, ParsingContext> {

        @Override
        public Expression infixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, Expression left, Expression right) {
            switch (name) {
                case "and":
                case "\u2227":
                    return new BinaryOperator('\u2227', left, right);
                case "or":
                case "\u2228":
                    return new BinaryOperator('\u2228', left, right);
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

        @Override
        public Expression prefixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, Expression argument) {
            return new UnaryOperator(name.equals("not") ? '\u00ac' : name.charAt(0), argument);
        }

        @Override
        public Expression suffixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, Expression argument) {
            return parseInvocation(parsingContext, tokenizer, argument);
        }


        @Override
        public Expression numberLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(Double.parseDouble(value));
        }

        @Override
        public Expression identifier(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {

            if (EMOJI_PATTERN.matcher(name).matches()) {
                return new Literal(name);
            }

            if (name.equals("true")) {
                return new Literal(Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new Literal(Boolean.FALSE);
            }
            if ("function".equals(name) || name.startsWith("function#")) {
                return parseFunction(parsingContext, tokenizer, extractId(name), null);
            }
            if (name.indexOf('#') != -1) {
                return new InstanceReference(name);
            }
            return new Identifier(name);
        }

        @Override
        public Expression group(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String paren, List<Expression> elements) {
            return elements.get(0);
        }

        @Override
        public Expression stringLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(ExpressionParser.unquote(value));
        }

        @Override
        public Expression apply(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, Expression to, String bracket, List<Expression> parameterList) {
            if (bracket.equals("(")) {
                return new UnresolvedInvocation(to, true, parameterList.toArray(new Expression[parameterList.size()]));
            }
            if (bracket.equals("[")) {
                return new UnresolvedArrayExpression(to, parameterList.toArray(new Expression[parameterList.size()]));
            }
            return super.apply(parsingContext, tokenizer, to, bracket, parameterList);
        }

        @Override
        public Expression primary(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {
            switch (name) {

                case "new": {
                    Expression expr = expressionParser.parse(parsingContext, tokenizer);
                    return new UnresolvedInvocation(new Identifier("new"), false, expr);
                }

                default:
                    return super.primary(parsingContext, tokenizer, name);
            }
        }

    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<Expression, ParsingContext> createExpressionParser() {
        ExpressionParser<Expression, ParsingContext> parser = new ExpressionParser<>(new Processor());

        parser.addGroupBrackets("(", null, ")");
        // parser.addGroupBrackets("[", ",", "]");

        // FIXME: Should be parser.
        // parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_PREFIX, "new");
        parser.addPrimary("new");
//        parser.addApplyBrackets(PRECEDENCE_PATH, "(", ",", ")");
        parser.addApplyBrackets(PRECEDENCE_PATH, "[", ",", "]");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");

        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_POWER, "^", "\u221a");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-", "\u221a", "\u00ac", "not");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/", "%");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");


        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=", "\u2264", "\u2265");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=", "==", "!=", "\u2260", "\u2261");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "and", "\u2227");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "or", "\u2228");

        parser.addOperators(ExpressionParser.OperatorType.SUFFIX, PRECEDENCE_PATH, "(");
        // FIXME
        // parser.addPrimary("on", "onchange");

        return parser;
    }
}
