package org.kobjects.codechat.api;

import java.io.IOException;
import java.io.StringWriter;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.lang.Instance;

public class Builtins {
    Environment environment;

    public Builtins(Environment environment) {
        this.environment = environment;
    }

    public String dump() {
        StringWriter sw = new StringWriter();
        try {
            environment.dump(sw);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sw.toString();
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

    public void pause() {
        environment.pause(true);
    }

    public void gc() {
        Runtime.getRuntime().gc();
    }

    public void unpause() {
        environment.pause(false);
    }
}
