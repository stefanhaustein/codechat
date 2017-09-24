package org.kobjects.codechat.lang;

import java.util.Collection;
import java.util.Map;

import jdk.nashorn.internal.ir.annotations.Ignore;
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
    public void serializeStub(AnnotatedStringBuilder asb) {
        asb.append(constant ? "const " : "variable ");
        asb.append(name);
        asb.append(": ");
        asb.append(type.toString());
        asb.append(";\n");
    }

    @Override
    public void serialize(AnnotatedStringBuilder asb, SerializationContext serializationContext) {
        if (builtin) {
            serializationContext.setState(this, SerializationContext.SerializationState.FULLY_SERIALIZED);
            return;
        }

        SerializationContext.SerializationState state = serializationContext.getState(this);
        if (state == SerializationContext.SerializationState.FULLY_SERIALIZED) {
            System.err.println("Double serialization of root identifier " + name);
            return;
        }

        serializationContext.serializeDependencies(asb, this);

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
            asb.append(name);
            asb.append(" = ");
            if (value instanceof Entity) {
                Entity entity = (Entity) value;
                SerializationContext.SerializationState valueState = serializationContext.getState(entity);
                switch (valueState) {
                    case UNVISITED:
                        entity.serialize(asb, serializationContext);
                        break;
                    case STUB_SERIALIZED:
                        serializationContext.enqueue(entity);
                        // FALLTHROUGH intended:
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
    public void getDependencies(Environment environment, DependencyCollector result) {
        if (value instanceof HasDependencies) {
            ((HasDependencies) value).getDependencies(environment, result);
        }
    }
}
