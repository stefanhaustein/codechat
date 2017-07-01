package org.kobjects.codechat.lang;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.expr.unresolved.UnresolvedArrayExpression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedBinaryOperator;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedFunctionExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedIdentifier;
import org.kobjects.codechat.expr.unresolved.UnresolvedInstanceReference;
import org.kobjects.codechat.expr.unresolved.UnresolvedLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedObjectLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedUnaryOperator;
import org.kobjects.codechat.statement.Assignment;
import org.kobjects.codechat.expr.unresolved.UnresolvedInvocation;
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
    public static final int PRECEDENCE_APPLY = 9;
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
    private final ExpressionParser<UnresolvedExpression, ParsingContext> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Statement parseBlock(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, boolean interactive, String... end) {
        Statement block = parseBlockLeaveEnd(parsingContext, tokenizer, interactive, end);
        tokenizer.nextToken();
        return block;
    }

    Statement parseBlockLeaveEnd(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, boolean interactive, String... end) {
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

            statements.add(parseStatement(parsingContext, tokenizer, interactive));

            for (String endToken : end) {
                if (tokenizer.currentValue.equals(endToken)) {
                    break outer;
                }
            }
            tokenizer.consume(";");
        }
        return statements.size() == 1 ? statements.get(0) : new Block(statements.toArray(new Statement[statements.size()]));
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
        if (tokenizer.tryConsume(":")) {   // TODO remove begin
            return parseBlock(parsingContext, tokenizer, false, "end");
        }
/*        if (tokenizer.tryConsume("{")) {
            return parseBlock(parsingContext, tokenizer,false, "}", "end");
        } */
        throw new RuntimeException("':' expected for body.");
    }

    Type parseType(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String typeName = tokenizer.consumeIdentifier();
        Type type = parsingContext.environment.resolveType(typeName);
        return type;
    }

    UnresolvedFunctionExpression parseFunction(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, int id, String name) {
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
        return new UnresolvedFunctionExpression(id, name, functionType, parameterNames.toArray(new String[parameterNames.size()]), bodyContext.getClosure(), body);
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

        Statement ifBody = parseBlockLeaveEnd(parsingContext, tokenizer, false, "end", /*"}",*/ "else", "elseif");

        Statement elseBody = null;

        if (tokenizer.tryConsume("elseif")) {
            elseBody = parseIf(parsingContext, tokenizer);
        } else if (tokenizer.tryConsume("else")) {
          /*  if (tokenizer.tryConsume("{")) {
                elseBody = parseBlock(parsingContext, tokenizer, false, "}", "end");
            } else { */
                tokenizer.tryConsume(":");
                elseBody = parseBlock(parsingContext, tokenizer, false, "end");
            //}
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
            UnresolvedFunctionExpression functionExpr = parseFunction(parsingContext, tokenizer, id, name);
            return new ExpressionStatement(functionExpr.resolve(parsingContext));
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
        /*     if (tokenizer.tryConsume("{")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, false, "}");
        }*/
        if (tokenizer.tryConsume("begin")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, false, "end");
        }


        if (tokenizer.currentValue.equals("move")) {
            System.out.println("******************* move ************************************");
        }

        UnresolvedExpression unresolved = expressionParser.parse(parsingContext, tokenizer);

        System.out.println("cp0: " + tokenizer.currentValue);

        if (unresolved instanceof UnresolvedBinaryOperator && ((UnresolvedBinaryOperator) unresolved).name == '=') {
            UnresolvedBinaryOperator op = (UnresolvedBinaryOperator) unresolved;
            Expression right = op.right.resolve(parsingContext);
            if (op.left instanceof UnresolvedIdentifier && parsingContext.parent == null && interactive) {
                String name = ((UnresolvedIdentifier) op.left).name;
                if (parsingContext.resolve(name) == null) {
                    environment.ensureRootVariable(name, right.getType());
                }
            }
            return new Assignment(op.left.resolve(parsingContext), right);
        }

        System.out.println("cp1: " + tokenizer.currentValue);

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

        if (unresolved instanceof UnresolvedIdentifier) {
            RootVariable var = parsingContext.environment.rootVariables.get(((UnresolvedIdentifier) unresolved).name);
            if (var != null && var.type instanceof FunctionType) {
                ArrayList<UnresolvedExpression> params = new ArrayList<>();
                while (!tokenizer.currentValue.equals("")
                        && !tokenizer.currentValue.equals(";")
                  //      && !tokenizer.currentValue.equals("}")
                 //       && !tokenizer.currentValue.equals("{")
                        && !tokenizer.currentValue.equals("else")
                        && !tokenizer.currentValue.equals("end")) {
                    UnresolvedExpression param = expressionParser.parse(parsingContext, tokenizer);
                    params.add(param);
                    tokenizer.tryConsume(",");
                }
                if (params.size() > 0 || var.value == null) {
                    unresolved = new UnresolvedInvocation(unresolved, false, params.toArray(new UnresolvedExpression[params.size()]));
                }
            }
        }

        Expression resolved = unresolved.resolve(parsingContext);
        return new ExpressionStatement(resolved);
    }

    UnresolvedObjectLiteral parseObjectLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, UnresolvedExpression base) {
        // tokenizer.consume("(");
        LinkedHashMap<String, UnresolvedExpression> elements = new LinkedHashMap<>();
        if (!tokenizer.tryConsume("}")) {
            do {
                String key = tokenizer.consumeIdentifier();
                tokenizer.consume(":");
                elements.put(key, expressionParser.parse(parsingContext, tokenizer));
            } while (tokenizer.tryConsume(","));
            tokenizer.consume("}");
        }
        return new UnresolvedObjectLiteral(base, elements);
    }

    Expression parseExpression(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        UnresolvedExpression unresolved = expressionParser.parse(parsingContext, tokenizer);
        return unresolved.resolve(parsingContext);
    }

    public Statement parse(ParsingContext parsingContext, String line) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(line);
        tokenizer.nextToken();
        Statement statement = parseBlock(parsingContext, tokenizer, true, "");
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
                expressionParser.getSymbols() , ":", "end", "else", ";", "}");
        tokenizer.identifierPattern = IDENTIFIER_PATTERN;
        return tokenizer;
    }



    public class Processor extends ExpressionParser.Processor<UnresolvedExpression, ParsingContext> {

        @Override
        public UnresolvedExpression infixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression left, UnresolvedExpression right) {
            char c;
            switch (name) {
                case "and":
                    c = '\u2227';
                    break;
                case "or":
                    c = '\u2228';
                    break;
                case "==":
                    c ='\u2261';
                    break;
                case "!=":
                    c = '\u2260';
                    break;
                case "<=":
                    c = '\u2264';
                    break;
                case ">=":
                    c = '\u2265';
                    break;
                case "\u00F7":
                    c = '/';
                    break;
                case "\u22C5":
                case "*":
                    c = '\u00d7';
                    break;
                default:
                    if (name.length() != 1) {
                        throw new IllegalArgumentException("Unrecognized operator: '" + name + "'");
                    }
                    c = name.charAt(0);
            }
            return new UnresolvedBinaryOperator(c, left, right);
        }

        @Override
        public UnresolvedExpression prefixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            return new UnresolvedUnaryOperator(name.equals("not") ? '\u00ac' : name.charAt(0), argument);
        }

        @Override
        public UnresolvedExpression suffixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            switch (name) {
                case "{": return parseObjectLiteral(parsingContext, tokenizer, argument);
                case "°": return new UnresolvedUnaryOperator('°', argument);
                default:
                    return super.suffixOperator(parsingContext, tokenizer, name, argument);
            }
        }


        @Override
        public UnresolvedExpression numberLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            return new UnresolvedLiteral(Double.parseDouble(value));
        }

        @Override
        public UnresolvedExpression identifier(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {

            if (EMOJI_PATTERN.matcher(name).matches()) {
                return new UnresolvedLiteral(name);
            }
            if (name.equals("true")) {
                return new UnresolvedLiteral(Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new UnresolvedLiteral(Boolean.FALSE);
            }
            if ("function".equals(name) || name.startsWith("function#")) {
                return parseFunction(parsingContext, tokenizer, extractId(name), null);
            }
            if (name.indexOf('#') != -1) {
                return new UnresolvedInstanceReference(name);
            }
            return new UnresolvedIdentifier(name);
        }

        @Override
        public UnresolvedExpression group(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String paren, List<UnresolvedExpression> elements) {
            return elements.get(0);
        }

        @Override
        public UnresolvedExpression stringLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            return new UnresolvedLiteral(ExpressionParser.unquote(value));
        }

        @Override
        public UnresolvedExpression apply(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, UnresolvedExpression to, String bracket, List<UnresolvedExpression> parameterList) {
            if (bracket.equals("(")) {
                return new UnresolvedInvocation(to, true, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            if (bracket.equals("[")) {
                return new UnresolvedArrayExpression(to, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            return super.apply(parsingContext, tokenizer, to, bracket, parameterList);
        }

        @Override
        public UnresolvedExpression primary(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {
            switch (name) {

                case "new": {
                    UnresolvedExpression expr = expressionParser.parse(parsingContext, tokenizer);
                    return new UnresolvedInvocation(new UnresolvedIdentifier("new"), false, expr);
                }

                default:
                    return super.primary(parsingContext, tokenizer, name);
            }
        }

    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<UnresolvedExpression, ParsingContext> createExpressionParser() {
        ExpressionParser<UnresolvedExpression, ParsingContext> parser = new ExpressionParser<>(new Processor());

        parser.addGroupBrackets("(", null, ")");
        // parser.addGroupBrackets("[", ",", "]");

        // FIXME: Should be parser.
        // parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_PREFIX, "new");
        parser.addPrimary("new");
//        parser.addApplyBrackets(PRECEDENCE_PATH, "(", ",", ")");
        parser.addApplyBrackets(PRECEDENCE_APPLY, "[", ",", "]");
        parser.addApplyBrackets(PRECEDENCE_APPLY, "(", ",", ")");
        parser.addOperators(ExpressionParser.OperatorType.SUFFIX, PRECEDENCE_APPLY, "{");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");

        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_POWER, "^", "\u221a");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-", "\u221a", "\u00ac", "not");
        parser.addOperators(ExpressionParser.OperatorType.SUFFIX, PRECEDENCE_SIGN, "°");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/", "\u00d7", "\u22C5", "%");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");


        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=", "\u2264", "\u2265");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=", "==", "!=", "\u2260", "\u2261");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "and", "\u2227");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "or", "\u2228");

        // FIXME
        // parser.addPrimary("on", "onchange");

        return parser;
    }
}
