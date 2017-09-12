package org.kobjects.codechat.lang;

import java.util.Collection;
import java.util.Map;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class RootVariable implements Entity, HasDependencies {
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
    public void serialize(AnnotatedStringBuilder asb, SerializationContext.Detail detail, SerializationContext serializationContext) {
        if (builtin) {
            return;
        }

        if (detail == SerializationContext.Detail.DECLARATION) {
            asb.append(constant ? "let " : "variable ");
            asb.append(name);
            asb.append(" : ").append(type.getName(), type instanceof Documented ? new DocumentedLink((Documented) type) : null);
            asb.append(";\n");
            return;
        }


        SerializationContext.Detail instanceDetail = null;
        SerializationContext.SerializationState newInstanceState = null;
        if (value instanceof Entity) {
            switch (serializationContext.getState((Entity) value)) {
                case UNVISITED:
                    instanceDetail = SerializationContext.Detail.DEFINITION;
                    newInstanceState = SerializationContext.SerializationState.STUB_SERIALIZED;
                    break;
                case PENDING:
                    instanceDetail = SerializationContext.Detail.DECLARATION;
                    newInstanceState = SerializationContext.SerializationState.FULLY_SERIALIZED;
                    break;
            }
        }

        if (detail == SerializationContext.Detail.DEFINITION
                && instanceDetail == SerializationContext.Detail.DEFINITION
                && type instanceof FunctionType && constant) {

            ((UserFunction) value).serializeWithName(asb, detail, serializationContext, name);

            serializationContext.setState((Entity) value, newInstanceState);
            return;
        }

        if (detail == SerializationContext.Detail.DEFINITION) {
            asb.append(constant ? "const " : "variable ");
        }

        asb.append(name);
        asb.append(" = ");

        if (instanceDetail == null) {
            Formatting.toLiteral(asb, value);
            asb.append(";\n");
        } else {
            ((Entity) value).serialize(asb, instanceDetail, serializationContext);
            serializationContext.setState((Entity) value, newInstanceState);
        }
    }

    @Override
    public void getDependencies(Environment environment, Collection<Entity> result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(environment, result);
        }
    }
}
