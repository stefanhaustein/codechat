package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.List;
import org.kobjects.codechat.annotation.AnnotationSpan;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;

public class TestShell implements EnvironmentListener {

    Environment environment = new Environment(this, null);
    List<String> output = new ArrayList<>();

    public Object eval(String line) {
        ParsingContext parsingContext = new ParsingContext(environment, ParsingContext.Mode.LOAD);

        Statement statement = environment.parse(parsingContext, line);

        if (statement instanceof ExpressionStatement) {
            Expression expression = ((ExpressionStatement) statement).expression;
            String s = expression.toString();
            return expression.eval(parsingContext.createEvaluationContext(environment));
        }
        return statement.eval(parsingContext.createEvaluationContext(environment));
    }

    @Override
    public void clearAll() {

    }

    @Override
    public void suspended(boolean paused) {
    }

    @Override
    public void setName(String name) {
    }

    @Override
    public void print(CharSequence s, Channel channel) {
        output.add(s.toString());
    }

    @Override
    public void loadExample(String name) {

    }

}
