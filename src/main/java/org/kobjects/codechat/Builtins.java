package org.kobjects.codechat;

import java.io.IOException;
import java.io.StringWriter;

public class Builtins {
    Environment environment;

    Builtins(Environment environment) {
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
        environment.pause();
    }

    public void unpause() {
        environment.unpause();
    }
}
