package org.kobjects.codechat.api;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public class Builtins {
    Environment environment;

    public Builtins(Environment environment) {
        this.environment = environment;
    }

    public void list() {
        StringWriter sw = new StringWriter();
        try {
            environment.dump(sw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String list = sw.toString();
        while (list.endsWith("\n")) {
            list = list.substring(0, list.length() - 1);
        }
        environment.environmentListener.print(list);
    }

    public Instance create(Class c) {
        return environment.instantiate(c);
    }

    public void save(String name) {
        environment.save(new File(environment.codeDir, name));
    }

    public void load(String name) {
        environment.load(new File(environment.codeDir, name));
        environment.environmentListener.setName(name);
    }

    public void clearAll() {
        environment.clearAll();
        environment.environmentListener.setName(null);
    }

    public void pause() {
        environment.pause(true);
    }

    public void print(double d) {
        environment.environmentListener.print(String.valueOf(d));
    }

    public void print(String s) {
        environment.environmentListener.print(String.valueOf(s));
    }

    public void print(boolean b) {
        environment.environmentListener.print(String.valueOf(b));
    }

    public void unpause() {
        environment.pause(false);
    }
}