package org.kobjects.codechat;

import java.util.ArrayList;
import java.util.List;
import org.kobjects.codechat.tree.FunctionCall;
import org.kobjects.codechat.tree.Identifier;
import org.kobjects.codechat.tree.Implicit;
import org.kobjects.codechat.tree.InfixOperator;
import org.kobjects.codechat.tree.Literal;
import org.kobjects.codechat.tree.Node;
import org.kobjects.codechat.tree.Property;
import org.kobjects.expressionparser.ExpressionParser;

public class Processor extends ExpressionParser.Processor<Node> {

    public static final int PRECEDENCE_PATH = 6;
    public static final int PRECEDENCE_POWER = 5;
    public static final int PRECEDENCE_SIGN = 4;
    public static final int PRECEDENCE_MULTIPLICATIVE = 3;
    public static final int PRECEDENCE_ADDITIVE = 2;
    public static final int PRECEDENCE_IMPLICIT = 1;
    public static final int PRECEDENCE_RELATIONAL = 0;

    @Override
    public Node infixOperator(ExpressionParser.Tokenizer tokenizer, String name, Node left, Node right) {
        if (".".equals(name)) {
            if (!(right instanceof Identifier)) {
                throw new RuntimeException("Property name required.");
            }
            return new Property(left, ((Identifier) right).name);
        }
        return new InfixOperator(name, left, right);
    }

    @Override
    public Node implicitOperator(ExpressionParser.Tokenizer tokenizer, boolean strong, Node left, Node right) {
        if (left instanceof Implicit) {
            Implicit li = (Implicit) left;
            List<Node> children = new ArrayList<>();
            for (int i = 0; i < li.children.length; i++) {
                children.add(li.children[i]);
            }
            children.add(right);
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
        return new Identifier(name);
    }

    @Override
    public Node group(ExpressionParser.Tokenizer tokenizer, String paren, List<Node> elements) {
        return elements.get(0);
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

    /**
     * Creates a parser for this processor with matching operations and precedences set up.
     */
    static ExpressionParser<Node> createParser() {
        ExpressionParser<Node> parser = new ExpressionParser<>(new Processor());
        parser.addCallBrackets("(", ",", ")");
        parser.addGroupBrackets("(", null, ")");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_PATH, ".");
        parser.addOperators(ExpressionParser.OperatorType.INFIX_RTL, PRECEDENCE_SIGN, "^");
        parser.addOperators(ExpressionParser.OperatorType.PREFIX, PRECEDENCE_SIGN, "+", "-");
       // parser.setImplicitOperatorPrecedence(true, 2);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_MULTIPLICATIVE, "*", "/");
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_ADDITIVE, "+", "-");
        parser.setImplicitOperatorPrecedence(false, PRECEDENCE_IMPLICIT);
        parser.addOperators(ExpressionParser.OperatorType.INFIX, PRECEDENCE_RELATIONAL, "=");
        return parser;
    }

}
