package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.CollectionType;
import org.kobjects.codechat.type.ListType;
import org.kobjects.codechat.type.SetType;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.Typed;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import static org.kobjects.codechat.lang.Formatting.toLiteral;


public class Collection implements Tuple {

    final CollectionType type;
    final java.util.Collection<Object> data;
    final Property<Double> size = new Property<Double>() {
        @Override
        public Double get() {
            return (double) data.size();
        }
    };

    public Collection(CollectionType type, Object... values) {
        this.type = type;

        if (type instanceof SetType) {
            data = new LinkedHashSet<>();
        } else if (type instanceof ListType) {
            data = new ArrayList<>(values.length);
        } else {
            throw new IllegalArgumentException("Unrecognized type: " + type);
        }
        for (Object value: values) {
            data.add(value);
        }
    }


    @Override
    public CollectionType getType() {
        return type;
    }

    @Override
    public Property getProperty(int index) {
        return size;
    }

    public Object get(int i) {
        return ((ArrayList) data).get(i);
    }


    public String toString() {
        StringBuilder sb = new StringBuilder(type.getName());
        sb.append('(');
        boolean first = true;
        for (Object value : data) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(toLiteral(value));
        }
        sb.append(')');
        return sb.toString();
    }
}
