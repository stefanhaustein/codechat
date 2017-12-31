package org.kobjects.codechat.parser;

import java.io.Reader;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.kobjects.codechat.expr.unresolved.UnresolvedArrayExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedBinaryOperator;
import org.kobjects.codechat.expr.unresolved.UnresolvedConstructor;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedFunctionExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedIdentifier;
import org.kobjects.codechat.expr.unresolved.UnresolvedInstanceReference;
import org.kobjects.codechat.expr.unresolved.UnresolvedLiteral;
import org.kobjects.codechat.expr.unresolved.UnresolvedMultiAssignment;
import org.kobjects.codechat.expr.unresolved.UnresolvedOnExpression;
import org.kobjects.codechat.expr.unresolved.UnresolvedUnaryOperator;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.OnInstance;
import org.kobjects.codechat.expr.unresolved.UnresolvedInvocation;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.statement.unresolved.UnresolvedAssignment;
import org.kobjects.codechat.statement.unresolved.UnresolvedBlock;
import org.kobjects.codechat.statement.unresolved.UnresolvedClass;
import org.kobjects.codechat.statement.unresolved.UnresolvedCountStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedExpressionStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedForStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedHelpStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedIfStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedScope;
import org.kobjects.codechat.statement.unresolved.UnresolvedSimpleStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedStatement;
import org.kobjects.codechat.statement.unresolved.UnresolvedVarDeclarationStatement;
import org.kobjects.codechat.type.FunctionType;
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
    private final ExpressionParser<UnresolvedExpression, Void> expressionParser = createExpressionParser();

    private UnresolvedExpression parseExpression(ExpressionParser.Tokenizer tokenizer) {
        return expressionParser.parse(null, tokenizer);
    }

    public Parser(Environment environment) {
        this.environment = environment;
    }

    public Statement parse(ParsingContext parsingContext, String code) {
        return parse(parsingContext, code, null);
    }

    public Statement parse(ParsingContext parsingContext, String code, List<Exception> errors) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(code);
        tokenizer.nextToken();
        UnresolvedStatement unresolved = parseBlock(tokenizer, true, "");
        while (tokenizer.tryConsume(";"))
            tokenizer.consume("");


        Statement result;
        if (unresolved instanceof UnresolvedBlock && errors != null) {
            UnresolvedBlock block = (UnresolvedBlock) unresolved;
            for (UnresolvedStatement statement : block.statements) {
                try {
                    statement.resolveTypes(parsingContext);
                } catch (Exception e) {
                    errors.add(e);
                }
            }
            ArrayList<Statement> list = new ArrayList<>();
            for (UnresolvedStatement statement : block.statements) {
                try {
                    list.add(statement.resolve(parsingContext));
                } catch (Exception e) {
                    errors.add(e);
                }
            }
            result = new Block(list.toArray(new Statement[list.size()]));
        } else {
            unresolved.resolveTypes(parsingContext);
            result = unresolved.resolve(parsingContext);
        }
        return result;
    }

    UnresolvedStatement parseBlock(ExpressionParser.Tokenizer tokenizer, boolean rootLevel, String... end) {
        UnresolvedStatement block = parseBlockLeaveEnd(tokenizer, rootLevel, end);
        tokenizer.nextToken();
        return block;
    }

    UnresolvedStatement parseBlockLeaveEnd(ExpressionParser.Tokenizer tokenizer, boolean interactive, String... end) {
        ArrayList<UnresolvedStatement> statements = new ArrayList<>();
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

            statements.add(parseStatement(tokenizer, interactive));

            for (String endToken : end) {
                if (tokenizer.currentValue.equals(endToken)) {
                    break outer;
                }
            }
            tokenizer.consume(";");
        }
        return statements.size() == 1 ? statements.get(0) : new UnresolvedBlock(statements.toArray(new UnresolvedStatement[statements.size()]));
    }

    UnresolvedCountStatement parseCount(ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();

        tokenizer.consume("to");

        int p0 = tokenizer.currentPosition;
        UnresolvedExpression expression = parseExpression(tokenizer);

        UnresolvedStatement body = parseBody(tokenizer);

        return new UnresolvedCountStatement(varName, expression, body);
    }

    UnresolvedForStatement parseFor(ExpressionParser.Tokenizer tokenizer) {
        String varName = tokenizer.consumeIdentifier();

        tokenizer.consume("in");

        UnresolvedExpression expression = parseExpression(tokenizer);
        UnresolvedStatement body = parseBody(tokenizer);
        return new UnresolvedForStatement(varName, expression, body);
    }

    UnresolvedStatement parseBody(ExpressionParser.Tokenizer tokenizer) {
        tokenizer.consume(":");
        return parseBlock(tokenizer, false, "end", "");
    }

    Type parseType(ExpressionParser.Tokenizer tokenizer) {
        String typeName = tokenizer.consumeIdentifier();
        Type type = environment.resolveType(typeName);
        return type;
    }

    FunctionType parseSignature(ExpressionParser.Tokenizer tokenizer, List<String> parameterNames) {
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
        if (tokenizer.tryConsume("->")) {
            returnType = parseType(tokenizer);
        } else {
            returnType = null;
        }
        return new FunctionType(returnType, parameterTypes.toArray(new Type[parameterTypes.size()]));
    }

    UnresolvedFunctionExpression parseFunction(ExpressionParser.Tokenizer tokenizer, int id, boolean returnsValue) {
        int start = tokenizer.currentPosition;

        ArrayList<String> parameterNames = new ArrayList<String>();
        FunctionType functionType = parseSignature(tokenizer, parameterNames);

        tokenizer.consume(":");
        UnresolvedStatement body = parseBlock(tokenizer, false,"end", "");
        return new UnresolvedFunctionExpression(start, tokenizer.currentPosition, id, functionType, parameterNames.toArray(new String[parameterNames.size()]), body);
    }

    UnresolvedOnExpression parseOnExpression(OnInstance.OnInstanceType type, ExpressionParser.Tokenizer tokenizer, int id) {
        int p0 = tokenizer.currentPosition;
        final UnresolvedExpression expression = parseExpression(tokenizer);
        final UnresolvedStatement body = parseBody(tokenizer);
        return new UnresolvedOnExpression(p0, tokenizer.currentPosition, type, id, expression, body);
    }

    UnresolvedIfStatement parseIf(ExpressionParser.Tokenizer tokenizer) {
        UnresolvedExpression condition = parseExpression(tokenizer);

        tokenizer.consume(":");

        UnresolvedStatement ifBody = parseBlockLeaveEnd(tokenizer, false, "end", /*"}",*/ "else", "elseif", "");

        UnresolvedStatement elseBody = null;

        if (tokenizer.tryConsume("elseif")) {
            elseBody = parseIf(tokenizer);
        } else if (tokenizer.tryConsume("else")) {
          /*  if (tokenizer.tryConsume("{")) {
                elseBody = parseBlock(parsingContext, tokenizer, false, "}", "end");
            } else { */
                tokenizer.tryConsume(":");
                elseBody = parseBlock(tokenizer, false, "end", "");
            //}
        } else {
            tokenizer.nextToken();
        }
        return new UnresolvedIfStatement(condition, ifBody, elseBody);
    }

    UnresolvedStatement parseVar(ExpressionParser.Tokenizer tokenizer, boolean constant, boolean rootLevel, String documentation) {
        String varName = tokenizer.consumeIdentifier();
        int p0 = tokenizer.currentPosition;
        Type type = null;
        if (tokenizer.tryConsume(":")) {
            type = parseType(tokenizer);
        }

        UnresolvedExpression init = null;
        if (tokenizer.tryConsume("=")) {
            init = parseExpression(tokenizer);

        }

        return processDeclaration(constant, rootLevel, varName, type, init, documentation);
    }

    UnresolvedVarDeclarationStatement processDeclaration(boolean constant, boolean rootLevel, String varName, Type type, UnresolvedExpression init, String documentation) {
        return new UnresolvedVarDeclarationStatement(constant, rootLevel, varName, type, init, documentation);
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

    UnresolvedClass.UnresolvedField parseField(ExpressionParser.Tokenizer tokenizer, String name) {
        UnresolvedExpression initializer = parseExpression(tokenizer);
        return new UnresolvedClass.UnresolvedField(name, initializer);
    }

    UnresolvedClass.UnresolvedMethod parseMethod(ExpressionParser.Tokenizer tokenizer, String name) {
        ArrayList<String> paramNames = new ArrayList<>();
        FunctionType functionType = parseSignature(tokenizer, paramNames);
        throw new RuntimeException("NYI");
    }

    UnresolvedClass parseClass(ExpressionParser.Tokenizer tokenizer) {
        String className = tokenizer.consumeIdentifier();
        tokenizer.consume(":");
        UnresolvedClass result = new UnresolvedClass(className);
        while(!tokenizer.tryConsume("end")) {
            String memberName = tokenizer.consumeIdentifier();
            if (tokenizer.tryConsume("=")) {
                result.addField(parseField(tokenizer, memberName));
            } else if (tokenizer.currentValue.equals("(")) {
                result.addMethod(parseMethod(tokenizer, memberName));
            } else {
                throw new ParsingException(tokenizer.currentPosition, tokenizer.currentPosition, "colon, opening brace or end expected.", null);
            }
        }
        return result;
    }

    UnresolvedStatement parseStatement(ExpressionParser.Tokenizer tokenizer, boolean rootLevel) {
        String documentation = consumeComments(tokenizer);
        if (tokenizer.tryConsume("count")) {
            return parseCount(tokenizer);
        }
        if (tokenizer.tryConsume("delete")) {
            return new UnresolvedSimpleStatement(UnresolvedSimpleStatement.Kind.DELETE, parseExpression(tokenizer));
        }
        if (tokenizer.tryConsume("class")) {
            if (!rootLevel) {
                throw new ParsingException(tokenizer.currentPosition - "class".length(), tokenizer.currentPosition, "class keyword permitted at root level only", null);
            }
            return parseClass(tokenizer);
        }
        if (tokenizer.currentValue.equals("function") || tokenizer.currentValue.startsWith("function#") ||
                tokenizer.currentValue.equals("func") || tokenizer.currentValue.startsWith("func#") ) {
            int p0 = tokenizer.currentPosition;
            int id = extractId(tokenizer.consumeIdentifier());
            String name = tokenizer.consumeIdentifier();

            UnresolvedFunctionExpression functionExpr = parseFunction(tokenizer, id, true);

            return processDeclaration(true, rootLevel, name, null, functionExpr, documentation);
        }
        if (tokenizer.currentValue.equals("proc") || tokenizer.currentValue.startsWith("proc#") ) {
            int p0 = tokenizer.currentPosition;
            int id = extractId(tokenizer.consumeIdentifier());
            String name = tokenizer.consumeIdentifier();
            UnresolvedFunctionExpression functionExpr = parseFunction(tokenizer, id, false);
            return processDeclaration(true, rootLevel, name, null, functionExpr, documentation);
        }
        if (tokenizer.tryConsume("for")) {
            return parseFor(tokenizer);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(tokenizer);
        }
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#")) {
            String name = tokenizer.consumeIdentifier();
            return new UnresolvedExpressionStatement(parseOnExpression(OnInstance.ON_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.currentValue.equals("onchange") || tokenizer.currentValue.startsWith("onchange#")) {
            String name = tokenizer.consumeIdentifier();
            return new UnresolvedExpressionStatement(parseOnExpression(OnInstance.ON_CHANGE_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.currentValue.equals("oninterval") || tokenizer.currentValue.startsWith("oninterval#")) {
            String name = tokenizer.consumeIdentifier();
            return new UnresolvedExpressionStatement(parseOnExpression(OnInstance.ON_INTERVAL_TYPE, tokenizer, extractId(name)));
        }
        if (tokenizer.tryConsume("var") || tokenizer.tryConsume("variable") || tokenizer.tryConsume("mutable")) {
            return parseVar(tokenizer, false, rootLevel, documentation);
        }
        if (tokenizer.tryConsume("let") || tokenizer.tryConsume("const")) {
            return parseVar(tokenizer, true, rootLevel, documentation);
        }
        if (tokenizer.tryConsume("return")) {
            return new UnresolvedSimpleStatement(UnresolvedSimpleStatement.Kind.RETURN, parseExpression(tokenizer));
        }
        /*     if (tokenizer.tryConsume("{")) {
            ParsingContext blockContext = new ParsingContext(parsingContext, false);
            return parseBlock(blockContext, tokenizer, false, "}");
        }*/

        if (tokenizer.tryConsume("help")) {
            String what = tokenizer.currentValue;
            if ("".equals(what) || ";".equals(what)) {
                return new UnresolvedHelpStatement(null);
            }
            tokenizer.nextToken();
            return new UnresolvedHelpStatement(what);
        }

        if (tokenizer.tryConsume("begin") || tokenizer.tryConsume("scope")) {
            tokenizer.tryConsume(":");
            return new UnresolvedScope(parseBlock(tokenizer, false, "end", ""));
        }


        UnresolvedExpression unresolved = parseExpression(tokenizer);
        int unresolvedPosition = tokenizer.currentPosition;

        if (unresolved instanceof UnresolvedBinaryOperator && ((UnresolvedBinaryOperator) unresolved).name.equals("=")) {
            UnresolvedBinaryOperator op = (UnresolvedBinaryOperator) unresolved;
            return new UnresolvedAssignment(op.left, op.right);
        }

        if (unresolved instanceof UnresolvedIdentifier) {
            ArrayList<UnresolvedExpression> params = new ArrayList<>();
            while (!tokenizer.currentValue.equals("")
                     && !tokenizer.currentValue.equals(";")
                      //      && !tokenizer.currentValue.equals("}")
                      //       && !tokenizer.currentValue.equals("{")
                      && !tokenizer.currentValue.equals("else")
                      && !tokenizer.currentValue.equals("end")) {
                  UnresolvedExpression param = parseExpression(tokenizer);
                  params.add(param);
                 tokenizer.tryConsume(",");
            }
            if (params.size() > 0) {
                unresolved = new UnresolvedInvocation(unresolvedPosition, unresolved, false, params.toArray(new UnresolvedExpression[params.size()]));
            }
        }
        return new UnresolvedExpressionStatement(unresolved);
    }


    public ExpressionParser.Tokenizer createTokenizer(String s) {
        return createTokenizer(new StringReader(s));
    }

    public ExpressionParser.Tokenizer createTokenizer(Reader reader) {
        ExpressionParser.Tokenizer tokenizer = new ExpressionParser.Tokenizer(
                new Scanner(reader),
                expressionParser.getSymbols() , ":", "end", "else", ";", "}", "->");
        tokenizer.identifierPattern = IDENTIFIER_PATTERN;
        tokenizer.insertSemicolons = true;
        return tokenizer;
    }

    private UnresolvedExpression parseMultiAssignment(ExpressionParser.Tokenizer tokenizer, UnresolvedExpression base) {
        LinkedHashMap<String, UnresolvedExpression> assignments = new LinkedHashMap<>();
        while (!tokenizer.tryConsume("end")) {
            while (tokenizer.tryConsume(";")) {
            }
            String propertyName = tokenizer.consumeIdentifier();
            tokenizer.consume("=");
            assignments.put(propertyName, expressionParser.parse(null, tokenizer));
            while (tokenizer.tryConsume(";")) {
            }
        }
        return new UnresolvedMultiAssignment(base, assignments, tokenizer.currentPosition);
    }



    public class Processor extends ExpressionParser.Processor<UnresolvedExpression, Void> {

        @Override
        public UnresolvedExpression infixOperator(Void context, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression left, UnresolvedExpression right) {
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
        public UnresolvedExpression prefixOperator(Void context, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            int start = argument.start - name.length();
            int end = argument.end;
            if (name.equals("new")) {
                if (argument instanceof UnresolvedIdentifier) {
                    return new UnresolvedConstructor(start, end, ((UnresolvedIdentifier) argument).name, -1);
                }
                if (argument instanceof UnresolvedInstanceReference) {
                    UnresolvedInstanceReference ref = (UnresolvedInstanceReference) argument;
                    return new UnresolvedConstructor(start, end, ref.typeName, ref.id);
                }
                throw new ParsingException(start, end, "Illegal argument for new.", null);
            }
            return new UnresolvedUnaryOperator(tokenizer.currentPosition - name.length(), tokenizer.currentPosition, name.equals("not") ? '\u00ac' : name.charAt(0), argument);
        }

        @Override
        public UnresolvedExpression suffixOperator(Void context, ExpressionParser.Tokenizer tokenizer, String name, UnresolvedExpression argument) {
            switch (name) {
                case "::": return parseMultiAssignment(tokenizer, argument);
                case "°": return new UnresolvedUnaryOperator(tokenizer.currentPosition - name.length(), tokenizer.currentPosition,'°', argument);
                default:
                    return super.suffixOperator(context, tokenizer, name, argument);
            }
        }

        @Override
        public UnresolvedExpression numberLiteral(Void context, ExpressionParser.Tokenizer tokenizer, String value) {
            int end = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int start = tokenizer.currentPosition - value.length();
            return new UnresolvedLiteral(start, end, Double.parseDouble(value));
        }

        @Override
        public UnresolvedExpression identifier(Void context, ExpressionParser.Tokenizer tokenizer, String name) {
            int end = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int start = end - name.length();

            // System.out.println("identifier start: " + start + " end: " + end);

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
                return parseFunction(tokenizer, extractId(name), true);
            }
            if ("proc".equals(name) || name.startsWith("proc#")) {
                return parseFunction(tokenizer, extractId(name), false);
            }
            if (name.indexOf('#') != -1) {
                return new UnresolvedInstanceReference(start, end, name);
            }
            return new UnresolvedIdentifier(start, end, name);
        }

        @Override
        public UnresolvedExpression group(Void context, ExpressionParser.Tokenizer tokenizer, String paren, List<UnresolvedExpression> elements) {
            return elements.get(0);
        }

        @Override
        public UnresolvedExpression stringLiteral(Void context, ExpressionParser.Tokenizer tokenizer, String value) {
            int start = tokenizer.currentPosition - tokenizer.leadingWhitespace.length();
            int end = start - value.length();
            return new UnresolvedLiteral(start, end, ExpressionParser.unquote(value));
        }

        @Override
        public UnresolvedExpression apply(Void context, ExpressionParser.Tokenizer tokenizer, UnresolvedExpression to, String bracket, List<UnresolvedExpression> parameterList) {
            if (bracket.equals("(")) {
                return new UnresolvedInvocation(tokenizer.currentPosition, to, true, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            if (bracket.equals("[")) {
                return new UnresolvedArrayExpression(tokenizer.currentPosition, to, parameterList.toArray(new UnresolvedExpression[parameterList.size()]));
            }
            return super.apply(context, tokenizer, to, bracket, parameterList);
        }

        @Override
        public UnresolvedExpression primary(Void context, ExpressionParser.Tokenizer tokenizer, String name) {
            switch (name) {

                case "new": {
                    UnresolvedExpression expr = expressionParser.parse(context, tokenizer);
                    return new UnresolvedInvocation(tokenizer.currentPosition,
                            new UnresolvedIdentifier(tokenizer.currentPosition - name.length(), tokenizer.currentPosition,
                                "new"), false, expr);
                }

                default:
                    return super.primary(context, tokenizer, name);
            }
        }
    }

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    ExpressionParser<UnresolvedExpression, Void> createExpressionParser() {
        ExpressionParser<UnresolvedExpression, Void> parser = new ExpressionParser<>(new Processor());

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
