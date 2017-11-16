package org.kobjects.codechat.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.RootVariableNode;
import org.kobjects.codechat.expr.unresolved.UnresolvedArrayExpression;
import org.kobjects.codechat.expr.OnExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedBinaryOperator;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedFunctionExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedIdentifier;
import org.kobjects.codechat.expr.unresolved.UnresolvedInstanceReference;
import org.kobjects.codechat.expr.unresolved.UnresolvedLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.expr.unresolved.UnresolvedUnaryOperator;
import org.kobjects.codechat.lang.Entity;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.LocalVariable;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.lang.UserFunction;
import org.kobjects.codechat.statement.Assignment;
import org.kobjects.codechat.expr.unresolved.UnresolvedInvocation;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.PropertyAccess;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.CountStatement;
import org.kobjects.codechat.statement.ForStatement;
import org.kobjects.codechat.statement.HelpStatement;
import org.kobjects.codechat.statement.ReturnStatement;
import org.kobjects.codechat.statement.LocalVarDeclarationStatement;
import org.kobjects.codechat.statement.DeleteStatement;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;
import org.kobjects.expressionparser.ExpressionParser;
import org.kobjects.expressionparser.ExpressionParser.ParsingException;

public class Parser {
    // public static final int PRECEDENCE_HASH = 8;
    public static final int PRECEDENCE_PREFIX = 11;
    public static final int PRECEDENCE_APPLY = 10;
    public static final int PRECEDENCE_PATH = 9;
    public static final int PRECEDENCE_POWER = 8;
    public static final int PRECEDENCE_SIGN = 7;
    public static final int PRECEDENCE_MULTIPLICATIVE = 6;
    public static final int PRECEDENCE_ADDITIVE = 5;
    public static final int PRECEDENCE_RELATIONAL = 4;
    public static final int PRECEDENCE_EQUALITY = 3;
    public static final int PRECEDENCE_AND = 2;
    public static final int PRECEDENCE_OR = 1;
    public static final int PRECEDENCE_ASSIGNMENT = 0;

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

    public Parser(Environment environment) {
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

        int p0 = tokenizer.currentPosition;
        Expression expression = parseExpression(parsingContext, tokenizer);

        if (!expression.getType().equals(Type.NUMBER)) {
            throw new ParsingException(p0, tokenizer.currentPosition, "Count expression must be a number.", null);

        }

        ParsingContext countParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = countParsingContext.addVariable(varName, Type.NUMBER, true);

        Statement body = parseBody(countParsingContext, tokenizer);

        return new CountStatement(counter, expression, body);
    }

    ForStatement parseFor(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();

        tokenizer.consume("in");

        Expression expression = parseExpression(parsingContext, tokenizer);

        if (!(expression.getType() instanceof CollectionType)) {
            throw new RuntimeException("For expression must be a list.");
        }
        Type elementType = ((CollectionType) expression.getType()).elementType;

        ParsingContext foreachParsingContext = new ParsingContext(parsingContext, false);

        LocalVariable counter = foreachParsingContext.addVariable(varName, elementType, true);

        Statement body = parseBody(foreachParsingContext, tokenizer);
        return new ForStatement(counter, expression, body);
    }

    Statement parseBody(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        tokenizer.consume(":");
        return parseBlock(parsingContext, tokenizer, false, "end", "");
    }

    Type parseType(ExpressionParser.Tokenizer tokenizer) {
        String typeName = tokenizer.consumeIdentifier();
        Type type = environment.resolveType(typeName);
        return type;
    }

    FunctionType parseSignature(ExpressionParser.Tokenizer tokenizer, boolean returnsValue, List<String> parameterNames) {
        tokenizer.consume("(");
        ArrayList<Type> parameterTypes = new ArrayList<Type>();
        if (!tokenizer.tryConsume(")")) {
            do {
                String paramName = tokenizer.consumeIdentifier();
                tokenizer.consume(":");
                Type type = parseType(tokenizer);
                if (parameterNames != null) {
                    parameterNames.add(paramName);
                }
                parameterTypes.add(type);
            } while (tokenizer.tryConsume(","));
            tokenizer.consume(")");
        }

        Type returnType;
        if (returnsValue) {
            tokenizer.consume(":");
            if (tokenizer.tryConsume("void") || tokenizer.tryConsume("Void")) {
                returnType = null;
            } else {
                returnType = parseType(tokenizer);
            }
        } else {
            returnType = null;
        }
        return new FunctionType(returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));
    }

    UnresolvedFunctionExpression parseFunction(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, int id, boolean returnsValue) {
        int start = tokenizer.currentPosition;
        ParsingContext bodyContext = new ParsingContext(parsingContext, true);

        ArrayList<String> parameterNames = new ArrayList<String>();
        FunctionType functionType = parseSignature(tokenizer, returnsValue, parameterNames);
        for (int i = 0; i < parameterNames.size(); i++) {
            bodyContext.addVariable(parameterNames.get(i), functionType.parameterTypes[i], true);
        }

        Statement body;
        if (tokenizer.tryConsume("fwd") || tokenizer.tryConsume(";") || tokenizer.tryConsume("")) {
            body = null;
        } else {
            tokenizer.consume(":");
            if (tokenizer.tryConsume("fwd")) {
                body = null;
            } else {
                body = parseBlock(bodyContext, tokenizer, false,"end", "");
            }
        }
        return new UnresolvedFunctionExpression(start, tokenizer.currentPosition, id, functionType, parameterNames.toArray(new String[parameterNames.size()]), bodyContext.getClosure(), body);
    }

    OnExpression parseOnExpression(ParsingContext parsingContext, OnInstance.OnInstanceType type, ExpressionParser.Tokenizer tokenizer, int id) {
        ParsingContext closureParsingContext = new ParsingContext(parsingContext, true);
        int p0 = tokenizer.currentPosition;
        final Expression expression = parseExpression(closureParsingContext, tokenizer);

        if (type == OnInstance.ON_CHANGE_TYPE) {
            if (!(expression instanceof PropertyAccess)) {
                throw new ParsingException(p0, tokenizer.currentPosition, "property expected.", null);
            }
        } else if (type == OnInstance.ON_TYPE) {
            if (!expression.getType().equals(Type.BOOLEAN)) {
                throw new ParsingException(p0, tokenizer.currentPosition, "Boolean expression expected.", null);
            }
        } else if (type == OnInstance.ON_INTERVAL_TYPE) {
           if (!expression.getType().equals(Type.NUMBER)) {
               throw new ParsingException(p0, tokenizer.currentPosition, "Boolean expression expected.", null);
           }
        } else {
            throw new IllegalArgumentException();
        }

        final Statement body = parseBody(closureParsingContext, tokenizer);
        OnExpression result = new OnExpression(type, id, expression, body, closureParsingContext.getClosure());
        return result;
    }

    IfStatement parseIf(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        Expression condition = parseExpression(parsingContext, tokenizer);

        tokenizer.consume(":");

        Statement ifBody = parseBlockLeaveEnd(parsingContext, tokenizer, false, "end", /*"}",*/ "else", "elseif", "");

        Statement elseBody = null;

        if (tokenizer.tryConsume("elseif")) {
            elseBody = parseIf(parsingContext, tokenizer);
        } else if (tokenizer.tryConsume("else")) {
          /*  if (tokenizer.tryConsume("{")) {
                elseBody = parseBlock(parsingContext, tokenizer, false, "}", "end");
            } else { */
                tokenizer.tryConsume(":");
                elseBody = parseBlock(parsingContext, tokenizer, false, "end", "");
            //}
        } else {
            tokenizer.nextToken();
        }
        return new IfStatement(condition, ifBody, elseBody);
    }

    Statement parseVar(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, boolean constant, boolean rootLevel, String documentation) {
        String varName = tokenizer.consumeIdentifier();
        int p0 = tokenizer.currentPosition;
        Type type = null;
        if (tokenizer.tryConsume(":")) {
            type = parseType(tokenizer);
        }

        Expression init = null;
        if (tokenizer.tryConsume("=")) {
            init = parseExpression(parsingContext, tokenizer);
            if (type == null) {
                type = init.getType();
            } else if (!type.isAssignableFrom(init.getType())) {
                throw new ParsingException(p0, tokenizer.currentPosition,
                        "Initializer type is not compatible with the declared type.", null);
            }
        }

        return processDeclaration(parsingContext, p0, tokenizer.currentPosition, constant, rootLevel, varName, type, init, documentation);
    }

    Statement processDeclaration(ParsingContext parsingContext, int p0, int currentPosition, boolean constant, boolean rootLevel, String varName, Type type, Expression init, String documentation) {
        if (rootLevel) {
            if (type == null) {
                throw new ParsingException(p0, currentPosition,
                        "Explicit type or initializer required for root constants and variables.", null);
            }
            RootVariable rootVariable = environment.declareRootVariable(varName, type, constant);
            rootVariable.documentation = documentation;
            Expression left = new RootVariableNode(rootVariable);
            if (init == null) {
                return new ExpressionStatement(left);
            }
            return new Assignment(left, init);
        }

        if (init == null) {
            throw new ParsingException(p0, currentPosition,
                    "Initializer required for local constants and variables.", null);
        }
        LocalVariable variable = parsingContext.addVariable(varName, type, constant);
        return new LocalVarDeclarationStatement(variable, init);
    }

    Instance parseNewStub(ExpressionParser.Tokenizer tokenizer) {
        String name = tokenizer.consumeIdentifier();
        int id = extractId(name);
        if (id != -1) {
            name = name.substring(0, name.indexOf('#'));
        }
        InstanceType type = (InstanceType) environment.resolveType(name);
        return (Instance) environment.getInstance(type, id, true);
    }

    RootVariable parseVarStub(ExpressionParser.Tokenizer tokenizer, boolean mutable) {
        String name = tokenizer.consumeIdentifier();
        Type type;
        Object value;
        if (tokenizer.tryConsume(":")) {
            type = parseType(tokenizer);
            value = null;
        } else if (tokenizer.tryConsume("=")) {
            if (tokenizer.tryConsume("new")) {
                type = parseType(tokenizer);
                value = null;
            } else {
                Expression expression = parseExpression(new ParsingContext(environment), tokenizer);
                if (!(expression instanceof Literal)) {
                    throw new RuntimeException("Literal expected for variable without explicit type.");
                }
                value = ((Literal) expression).value;
                type = Type.of(value);
            }
        } else {
            throw new RuntimeException("':' or '=' expected after varname.");
        }
        RootVariable var = new RootVariable();
        var.name = name;
        var.type = type;
        var.constant = !mutable;
        var.value = value;
        var.documentation = consumeComments(tokenizer);
        environment.rootVariables.put(name, var);

        return var;
    }

    UserFunction parseFunctionStub(ExpressionParser.Tokenizer tokenizer, int id, boolean returnsValue) {
        String name = tokenizer.consumeIdentifier();
        FunctionType type = parseSignature(tokenizer, returnsValue, null);
        RootVariable var = new RootVariable();
        UserFunction value = new UserFunction(environment, type, id);
        var.name = name;
        var.type = type;
        var.constant = true;
        environment.rootVariables.put(name, var);
        return value;
    }

    public String consumeComments(ExpressionParser.Tokenizer tokenizer) {
        String documentation = tokenizer.consumeComments();
        if (documentation != null && documentation.length() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : documentation.split("\n")) {
                s = s.trim();
                if (s.startsWith("# ")) {
                    s = s.substring(2);
                } else if (s.startsWith("#")) {
                    s = s.substring(1);
                }
                if (s.isEmpty()) {
                    sb.append("\n\n");
                } else {
                    sb.append(s);
                    sb.append(' ');
                }
            }
            documentation = sb.toString();
        }
        return documentation == null || documentation.trim().isEmpty() ? null : documentation;
    }

    public Entity parseStub(String content) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(content);
        tokenizer.nextToken();
        Entity result;
        if (tokenizer.tryConsume("variable")) {
            result = parseVarStub(tokenizer, true);
        } else if (tokenizer.tryConsume("let")) {
            result = parseVarStub(tokenizer, false);
        } else if (tokenizer.tryConsume("func")) {
            result = parseFunctionStub(tokenizer, -1, true);
        } else if (tokenizer.tryConsume("proc")) {
            result = parseFunctionStub(tokenizer, -1, false);
        } else if (tokenizer.tryConsume("new")) {
            result = parseNewStub(tokenizer);
            while (tokenizer.tryConsume(";")) {
            }
            if (tokenizer.currentValue.isEmpty()) {
                return result;
            }
        } else if (tokenizer.tryConsume("begin")) {
            String[] lines = content.split("\n");
            int found = 0;
            for (int i = 1; i < lines.length; i++) {
                if (!lines[i].startsWith("  let ") && !lines[i].startsWith("   ")) {
                    found = i;
                    break;
                }
            }
            if (found == 0) {
                throw new RuntimeException("Closure main content not found.");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = found; i < lines.length; i++) {
                sb.append(lines[i]).append('\n');
            }
            result = parseStub(sb.toString());
        } else if (tokenizer.currentValue.startsWith("on")) {
            String name = tokenizer.consumeIdentifier();
            int id = extractId(name);
            if (id != -1) {
                name = name.substring(0, name.indexOf('#'));
            }
            name = "On" + (name.length() > 2 ? Character.toUpperCase(name.charAt(2)) + name.substring(3) : "");
            InstanceType type = environment.resolveInstanceType(name);
            result = environment.instantiate(type, id);
        } else {
            throw new RuntimeException("Unrecognized token: '" + tokenizer.currentValue + "' in '" + content + "'");
        }

        if ((result instanceof RootVariable) && ((RootVariable) result).value != null) {
            return result;
        }
        result.setUnparsed(content);
        return result;

    }


    Statement parseStatement(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, boolean interactive) {
        String documentation = consumeComments(tokenizer);
        if (tokenizer.tryConsume("count")) {
            return parseCount(parsingContext, tokenizer);
        }
        if (tokenizer.tryConsume("delete")) {
            return new DeleteStatement(parseExpression(parsingContext, tokenizer), parsingContext);
        }
        if (tokenizer.currentValue.equals("function") || tokenizer.currentValue.startsWith("function#") ||
                tokenizer.currentValue.equals("func") || tokenizer.currentValue.startsWith("func#") ) {
            int p0 = tokenizer.currentPosition;
            int id = extractId(tokenizer.consumeIdentifier());
            String name = tokenizer.consumeIdentifier();

            Expression functionExpr = parseFunction(parsingContext, tokenizer, id, true).resolve(parsingContext, null);

            return processDeclaration(parsingContext, p0, tokenizer.currentPosition, true, interactive, name, functionExpr.getType(), functionExpr, documentation);
        }
        if (tokenizer.currentValue.equals("proc") || tokenizer.currentValue.startsWith("proc#") ) {
            int p0 = tokenizer.currentPosition;
            int id = extractId(tokenizer.consumeIdentifier());
            String name = tokenizer.consumeIdentifier();
            Expression functionExpr = parseFunction(parsingContext, tokenizer, id, false).resolve(parsingContext, null);
            return processDeclaration(parsingContext, p0, tokenizer.currentPosition, true, interactive, name, functionExpr.getType(), functionExpr, documentation);
        }
        if (tokenizer.tryConsume("for")) {
            return parseFor(parsingContext, tokenizer);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(parsingContext, tokenizer);
        }
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#")) {
            String name = tokenizer.consumeIdentifier();
            return new ExpressionStatement(parseOnExpression(parsingContext, OnInstance.ON_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.currentValue.equals("onchange") || tokenizer.currentValue.startsWith("onchange#")) {
            String name = tokenizer.consumeIdentifier();
            return new ExpressionStatement(parseOnExpression(parsingContext, OnInstance.ON_CHANGE_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.currentValue.equals("oninterval") || tokenizer.currentValue.startsWith("oninterval#")) {
            String name = tokenizer.consumeIdentifier();
            return new ExpressionStatement(parseOnExpression(parsingContext, OnInstance.ON_INTERVAL_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.tryConsume("var") || tokenizer.tryConsume("variable") || tokenizer.tryConsume("mutable")) {
            return parseVar(parsingContext, tokenizer, false, interactive, documentation);
        }
        if (tokenizer.tryConsume("let") || tokenizer.tryConsume("const")) {
            return parseVar(parsingContext, tokenizer, true, interactive, documentation);
        }
        if (tokenizer.tryConsume("return")) {
            return new ReturnStatement(parseExpression(parsingContext, tokenizer));
        }
        /*     if (tokenizer.tryConsume("{")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, false, "}");
        }*/

        if (tokenizer.tryConsume("help")) {
            String what = tokenizer.currentValue;
            if ("".equals(what) || ";".equals(what)) {
                return new HelpStatement(null);
            }
            tokenizer.nextToken();
            return new HelpStatement(what);
        }

        if (tokenizer.tryConsume("begin")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, false, "end", "");
        }

        UnresolvedExpression unresolved = expressionParser.parse(parsingContext, tokenizer);
        int unresolvedPosition = tokenizer.currentPosition;

        if (unresolved instanceof UnresolvedBinaryOperator && ((UnresolvedBinaryOperator) unresolved).name.equals("=")) {
            UnresolvedBinaryOperator op = (UnresolvedBinaryOperator) unresolved;
            if (op.left instanceof UnresolvedIdentifier && parsingContext.parent == null && interactive) {
                String name = ((UnresolvedIdentifier) op.left).name;
                if (parsingContext.resolve(name) == null) {
                    Expression right = op.right.resolve(parsingContext, null);
                    environment.declareRootVariable(name, right.getType(), true).documentation = documentation;
                }
            }
            Expression left = op.left.resolve(parsingContext, null);
            return new Assignment(left, op.right.resolve(parsingContext, left.getType()));
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
                unresolved = new UnresolvedInvocation(unresolvedPosition, unresolved, false, params.toArray(new UnresolvedExpression[params.size()]));
            }
        }

        Expression resolved = unresolved.resolve(parsingContext, null);
        return new ExpressionStatement(resolved);
    }

    Expression parseExpression(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer) {
        UnresolvedExpression unresolved = expressionParser.parse(parsingContext, tokenizer);
        return unresolved.resolve(parsingContext, null);
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
        tokenizer.insertSemicolons = true;
        return tokenizer;
    }



    public class Processor extends ExpressionParser.Processor<UnresolvedExpression, ParsingContext> {

        @Override
        public UnresolvedExpression infixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression left, UnresolvedExpression right) {
            switch (name) {
                case "and":
                    name = "\u2227";
                    break;
                case "or":
                    name = "\u2228";
                    break;
                case "==":
                    name = "\u2261";
                    break;
                case "!=":
                    name = "\u2260";
                    break;
                case "<=":
                    name = "\u2264";
                    break;
                case ">=":
                    name = "\u2265";
                    break;
                case "\u00F7":
                    name = "/";
                    break;
                case "\u22C5":
                case "*":
                    name = "\u00d7";
                    break;
                case "\u22C5=":
                case "*=":
                    name = "\u00d7=";
                    break;
                case "\u00F7=":
                    name = "/=";
                    break;
            }
            return new UnresolvedBinaryOperator(name, left, right);
        }

        @Override
        public UnresolvedExpression prefixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            if (name.equals("new")) {
                return new UnresolvedInvocation(tokenizer.currentPosition,
                        new UnresolvedIdentifier(tokenizer.currentPosition - name.length(), tokenizer.currentPosition,
                                "new"), false, argument);
            }
            return new UnresolvedUnaryOperator(tokenizer.currentPosition - name.length(), tokenizer.currentPosition, name.equals("not") ? '\u00ac' : name.charAt(0), argument);
        }

        @Override
        public UnresolvedExpression suffixOperator(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            switch (name) {
                case "::": return parseMultiAssignment(parsingContext, tokenizer, argument);
                case "°": return new UnresolvedUnaryOperator(tokenizer.currentPosition - name.length(), tokenizer.currentPosition,'°', argument);
                default:
                    return super.suffixOperator(parsingContext, tokenizer, name, argument);
            }
        }


        @Override
        public UnresolvedExpression numberLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            int end = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int start = tokenizer.currentPosition - value.length();
            return new UnresolvedLiteral(start, end, Double.parseDouble(value));
        }

        @Override
        public UnresolvedExpression identifier(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {
            int end = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int start = end - name.length();

            System.out.println("identifier start: " + start + " end: " + end);

            if (EMOJI_PATTERN.matcher(name).matches()) {
                return new UnresolvedLiteral(start, end, name);
            }
            if (name.equals("true")) {
                return new UnresolvedLiteral(start, end, Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new UnresolvedLiteral(start, end, Boolean.FALSE);
            }
            if ("function".equals(name) || name.startsWith("function#") ||
                    "func".equals(name) || name.startsWith("func#")) {
                return parseFunction(parsingContext, tokenizer, extractId(name), true);
            }
            if ("proc".equals(name) || name.startsWith("proc#")) {
                return parseFunction(parsingContext, tokenizer, extractId(name), false);
            }
            if (name.indexOf('#') != -1) {
                return new UnresolvedInstanceReference(start, end, name);
            }
            return new UnresolvedIdentifier(start, end, name);
        }

        @Override
        public UnresolvedExpression group(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String paren, List<UnresolvedExpression> elements) {
            return elements.get(0);
        }

        @Override
        public UnresolvedExpression stringLiteral(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String value) {
            int start = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int end = start - value.length();
            return new UnresolvedLiteral(start, end, ExpressionParser.unquote(value));
        }

        @Override
        public UnresolvedExpression apply(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, UnresolvedExpression to, String bracket, List<UnresolvedExpression> parameterList) {
            if (bracket.equals("(")) {
                return new UnresolvedInvocation(tokenizer.currentPosition, to, true, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            if (bracket.equals("[")) {
                return new UnresolvedArrayExpression(tokenizer.currentPosition, to, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            return super.apply(parsingContext, tokenizer, to, bracket, parameterList);
        }

        @Override
        public UnresolvedExpression primary(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, String name) {
            switch (name) {

                case "new": {
                    UnresolvedExpression expr = expressionParser.parse(parsingContext, tokenizer);
                    return new UnresolvedInvocation(tokenizer.currentPosition,
                            new UnresolvedIdentifier(tokenizer.currentPosition - name.length(), tokenizer.currentPosition,
                                "new"), false, expr);
                }

                default:
                    return super.primary(parsingContext, tokenizer, name);
            }
        }
    }

    private UnresolvedExpression parseMultiAssignment(ParsingContext parsingContext, ExpressionParser.Tokenizer tokenizer, UnresolvedExpression base) {
        LinkedHashMap<String, UnresolvedExpression> assignments = new LinkedHashMap<>();
        while (!tokenizer.tryConsume("end")) {
            while (tokenizer.tryConsume(";")) {
            }
            String propertyName = tokenizer.consumeIdentifier();
            tokenizer.consume("=");
            assignments.put(propertyName, expressionParser.parse(parsingContext, tokenizer));
            while (tokenizer.tryConsume(";")) {
            }
        }
        return new UnresolvedMultiAssignment(base, assignments, tokenizer.currentPosition);
    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<UnresolvedExpression, ParsingContext> createExpressionParser() {
        ExpressionParser<UnresolvedExpression, ParsingContext> parser = new ExpressionParser<>(new Processor());

        parser.addGroupBrackets("(", null, ")");
        // parser.addGroupBrackets("[", ",", "]");

        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_PREFIX, "new");
        // parser.addPrimary("new");
//        parser.addApplyBrackets(PRECEDENCE_PATH, "(", ",", ")");
        parser.addApplyBrackets(PRECEDENCE_APPLY, "[", ",", "]");
        parser.addApplyBrackets(PRECEDENCE_APPLY, "(", ",", ")");
        parser.addOperators(ExpressionParser.OperatorType.SUFFIX, PRECEDENCE_APPLY, "{", "::");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");

        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_POWER, "^", "\u221a");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-", "\u221a", "\u00ac", "not");
        parser.addOperators(ExpressionParser.OperatorType.SUFFIX, PRECEDENCE_SIGN, "°");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/", "\u00d7", "\u00f7", "\u22C5", "%");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");


        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "<", "<=", ">", ">=", "\u2264", "\u2265");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_EQUALITY, "=", "==", "!=", "\u2260", "\u2261");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "and", "\u2227");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_AND, "or", "\u2228");

        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ASSIGNMENT, "+=", "-=", "*=", "\u00d7=", "\u00f7=", "\u22C5=", "/=");


        // FIXME
        // parser.addPrimary("on", "onchange");

        return parser;
    }
}
