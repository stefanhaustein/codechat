package org.kobjects.codechat.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.EnvironmentListener;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;

public class Shell implements EnvironmentListener {
    Environment environment;

    Shell() {
        environment = new Environment(this, new File("."));
    }

    public static void main(String[] args) throws IOException {
        new Shell().run();
    }

    public void run() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) {
                break;
            }
            process(line);
        }
    }

    private void process(String code) {
        ParsingContext parsingContext = new ParsingContext(environment, ParsingContext.Mode.INTERACTIVE);
        try {
            Statement statement = environment.parse(parsingContext, code);

            if (statement instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) statement).expression;
                String s = expression.toString();
                print(s, Channel.OUTPUT);
                Object result = expression.eval(parsingContext.createEvaluationContext(environment));
                if (expression.getType() == null) {
                    print("ok", EnvironmentListener.Channel.OUTPUT);
                } else {
                    print(Formatting.toLiteral(result), EnvironmentListener.Channel.OUTPUT);
                }
            } else {
                print(statement.toString(), EnvironmentListener.Channel.OUTPUT);
                statement.eval(parsingContext.createEvaluationContext(environment));
                print("ok", EnvironmentListener.Channel.OUTPUT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearAll() {
        System.out.println("\u000c");
    }

    @Override
    public void suspended(boolean paused) {
        print(paused ? "(suspended)" : "(resumed)", EnvironmentListener.Channel.OUTPUT);
    }

    @Override
    public void setName(String name) {
        print("Name set to: '" + name + "'", EnvironmentListener.Channel.OUTPUT);
    }


    @Override
    public void print(CharSequence s, EnvironmentListener.Channel channel) {
        System.out.println(s);
    }

    @Override
    public void showError(CharSequence s) {
        System.err.println(s);
    }

    @Override
    public void edit(String s) {
        System.out.println(s);
    }

    @Override
    public void loadExample(String name) {
        throw new UnsupportedOperationException();
    }
}
