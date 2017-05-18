package org.kobjects.codechat.shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Formatting;
import org.kobjects.codechat.lang.ParsingContext;
import org.kobjects.codechat.lang.Type;
import org.kobjects.codechat.statement.ExpressionStatement;
import org.kobjects.codechat.statement.Statement;

public class Shell implements Environment.EnvironmentListener {
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

    private void process(String line) {
        ParsingContext parsingContext = new ParsingContext(environment);
        boolean printed = false;
        try {
            Statement statement = environment.parse(line, parsingContext);

            if (statement instanceof ExpressionStatement) {
                Expression expression = ((ExpressionStatement) statement).expression;
                String s = expression.toString();
                print(s);
                printed = true;
                Object result = expression.eval(parsingContext.createEvaluationContext());
                if (Type.VOID.equals(expression.getType())) {
                    print("ok");
                } else {
                    print(Formatting.toLiteral(result));
                }
            } else {
                print(statement.toString());
                printed = true;
                statement.eval(parsingContext.createEvaluationContext());
                print("ok");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void paused(boolean paused) {
        print(paused ? "(paused)" : "(unpaused)");
    }

    @Override
    public void setName(String name) {
        print("Name set to: '" + name + "'");
    }

    @Override
    public void print(String s) {
        System.out.println(s);
    }
}
