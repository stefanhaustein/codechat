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
import org.kobjects.codechat.expr.FunctionCall;
import org.kobjects.codechat.expr.Identifier;
import org.kobjects.codechat.expr.Implicit;
import org.kobjects.codechat.expr.InfixOperator;
import org.kobjects.codechat.expr.Reference;
import org.kobjects.codechat.expr.Literal;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.Property;
import org.kobjects.codechat.statement.Block;
import org.kobjects.codechat.statement.Delete;
import org.kobjects.codechat.statement.IfStatement;
import org.kobjects.codechat.statement.OnStatement;
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

    private static Pattern IDENTIFIER_PATTERN = Pattern.compile(
            "\\G\\s*[\\p{Alpha}_$][\\p{Alpha}_$\\d]*(#\\d+)?");

    private final Environment environment;
    private final ExpressionParser<Expression> expressionParser = createExpressionParser();

    Parser(Environment environment) {
        this.environment = environment;
    }

    Block parseBlock(ExpressionParser.Tokenizer tokenizer, Scope scope, String end) {
        ArrayList<Evaluable> statements = new ArrayList<>();
        while(true) {
            while(tokenizer.tryConsume(";")) {
                //
            }
            if (tokenizer.tryConsume(end)) {
                break;
            }
            statements.add(parseStatement(tokenizer, scope));
        }
        return new Block(statements.toArray(new Evaluable[statements.size()]));
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


    Evaluable parseStatement(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        if (tokenizer.currentValue.equals("on") || tokenizer.currentValue.startsWith("on#")) {
            String name = tokenizer.consumeIdentifier();
            return parseOn(tokenizer, name);
        }
        if (tokenizer.tryConsume("if")) {
            return parseIf(tokenizer, scope);
        }

        if (tokenizer.tryConsume("delete")) {
            return new Delete(parseExpression(tokenizer, scope));
        }
        return parseExpression(tokenizer, scope);
    }

    Expression parseExpression(ExpressionParser.Tokenizer tokenizer, Scope scope) {
        Expression unresolved = expressionParser.parse(tokenizer);

        if (scope == environment.rootScope && unresolved instanceof InfixOperator) {
            InfixOperator op = (InfixOperator) unresolved;
            if (op.name.equals("=") && op.left instanceof Identifier) {
                Expression right = op.right.resolve(scope);

                scope.ensureVariable(((Identifier) op.left).name, right.getType());

                return new Assignment(op.left.resolve(scope), right);
            }
        }
        return unresolved.resolve(scope);
    }

    public Evaluable parse(String line) {
        ExpressionParser.Tokenizer tokenizer = createTokenizer(line);
        tokenizer.nextToken();
        return parseStatement(tokenizer, environment.rootScope);
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
                    return new Property(left, right);
                default:
                    return new InfixOperator(name, left, right);
            }
        }

        @Override
        public Expression implicitOperator(ExpressionParser.Tokenizer tokenizer, boolean strong, Expression left, Expression right) {
            if (left instanceof Implicit) {
                Implicit li = (Implicit) left;
                Expression[] children = new Expression[li.children.length + 1];
                System.arraycopy(li.children, 0, children, 0, li.children.length);
                children[li.children.length] = right;
                return new Implicit(children);
            }
            return new Implicit(left, right);
        }

        @Override
        public Expression prefixOperator(ExpressionParser.Tokenizer tokenizer, String name, Expression argument) {
            return new InfixOperator(name, argument, null);
        }

        @Override
        public Expression numberLiteral(ExpressionParser.Tokenizer tokenizer, String value) {
            return new Literal(Double.parseDouble(value));
        }

        @Override
        public Expression identifier(ExpressionParser.Tokenizer tokenizer, String name) {
            if (name.equals("true")) {
                return new Literal(Boolean.TRUE);
            }
            if (name.equals("false")) {
                return new Literal(Boolean.FALSE);
            }
            if (name.indexOf('#') != -1) {
                return new Reference(name);
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

        /**
         * Delegates function calls to Math via reflection.
         */
        @Override
        public Expression call(ExpressionParser.Tokenizer tokenizer, String identifier, String bracket, List<Expression> arguments) {
            return new FunctionCall(identifier, arguments);
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
