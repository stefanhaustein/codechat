package org.kobjects.codechat.lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.kobjects.codechat.type.Type;

public class RootVariable implements Dependency, HasDependencies {
    public String name;
    public Type type;
    public Object value;
    public boolean constant;

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" = ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, Instance.Detail detail) {
        asb.append(name);
        asb.append(" = ");
        asb.append(Formatting.toLiteral(value));
        asb.append(";\n");
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        if (value instanceof Dependency) {
            result.add((Dependency) value);
        } else if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(environment, result);
        }
    }
}
