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
        environment.save(name);
    }

    public void load(String name) {
        environment.load(name);
    }

    public void clearAll() {
        environment.clearAll();
        environment.environmentListener.setName("CodeChat");
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

    public Screen screen() {
        return environment.screen;
    }

    public double round(double d) {
        return Math.round(d);
    }

    public double random() {
        return Math.random();
    }
}
