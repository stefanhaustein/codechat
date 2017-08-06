package org.kobjects.codechat.lang;

import java.util.Collection;
import java.util.Map;

import org.kobjects.codechat.type.Type;

public class RootVariable implements Dependency, HasDependencies {
    public String name;
    public Type type;
    public Object value;
    public boolean constant;
    public boolean builtin;

    public void dump(StringBuilder sb) {
        sb.append(name);
        sb.append(" = ");
        sb.append(Formatting.toLiteral(value));
        sb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, Instance.Detail detail, Map<Dependency, Environment.SerializationState> serializationStateMap) {
        if (builtin) {
            return;
        }
        if (detail == Instance.Detail.DECLARATION) {
            asb.append(constant ? "let " : "mutable ");
            asb.append(name);
            asb.append(" : ").append(type.getName(), type);
            asb.append(";\n");
            return;
        }


        Instance.Detail instanceDetail = null;
        Environment.SerializationState newInstanceState = null;
        if (value instanceof Dependency) {
            Environment.SerializationState state = serializationStateMap.get((Dependency) value);
            if (state == null) {
                instanceDetail = Instance.Detail.DEFINITION;
                newInstanceState = Environment.SerializationState.STUB_SERIALIZED;
            } else if (state == Environment.SerializationState.PENDING) {
                instanceDetail = Instance.Detail.DECLARATION;
                newInstanceState = Environment.SerializationState.FULLY_SERIALIZED;
            }
        }

        if (detail == Instance.Detail.DEFINITION) {
            asb.append(constant ? "let " : "variable ");
        }

        asb.append(name);
        asb.append(" = ");

        if (instanceDetail == null) {
            asb.append(Formatting.toLiteral(value));
            asb.append(";\n");
        } else {
            ((Dependency) value).serialize(asb, instanceDetail, serializationStateMap);
            serializationStateMap.put((Dependency) value, newInstanceState);
        }
    }

    @Override
    public void getDependencies(Environment environment, Collection<Dependency> result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(environment, result);
        }
    }
}
