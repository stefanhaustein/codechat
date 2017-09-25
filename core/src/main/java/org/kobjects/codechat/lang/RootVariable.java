package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.EntityLink;
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
    public void serializeStub(AnnotatedStringBuilder asb) {
        asb.append(constant ? "const " : "variable ");
        asb.append(name);
        asb.append(": ");
        asb.append(type.toString());
        asb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        SerializationContext.SerializationState state = serializationContext.getState(this);
        if (state == SerializationContext.SerializationState.FULLY_SERIALIZED) {
            System.err.println("Double serialization of root identifier " + name);
            return;
        }

        serializationContext.serializeDependencies(asb, this);

        if (builtin || value == null) {
            serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
            return;
        }

        serializationContext.setState(this, SerializationContext.SerializationState.STUB_SERIALIZED);

        if (value instanceof UserFunction && constant) {
            UserFunction fn = (UserFunction) value;
            serializationContext.setState(fn, SerializationContext.SerializationState.STUB_SERIALIZED);
            fn.serializeWithName(asb, name, true);
            serializationContext.setState(fn, SerializationContext.SerializationState.FULLY_SERIALIZED);
        } else {
            if (state == SerializationContext.SerializationState.UNVISITED) {
                asb.append(constant ? "let " : "variable ");
            }
            if (value instanceof Entity) {
                Entity entity = (Entity) value;

                switch (serializationContext.getState(entity)) {
                    case UNVISITED:
                        asb.append(name, new EntityLink(this));
                        asb.append(" = ");
                        entity.serialize(asb, serializationContext);
                        break;
                    case STUB_SERIALIZED:
                        entity.serialize(asb, serializationContext);
                        break;
                    default:
                        Formatting.toLiteral(asb, value);
                        asb.append(";\n");
                }
            } else {
                Formatting.toLiteral(asb, value);
                asb.append(";\n");
            }
        }

        serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
    }

    @Override
    public void getDependencies(DependencyCollector result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(result);
        }
    }
}
