package org.kobjects.codechat.parser;

import java.util.TreeMap;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.parser.ParsingEnvironment;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

/**
 * Environment used for syntax checking.
 */
public class FakeEnvironment implements ParsingEnvironment {

    private final ParsingEnvironment environment;
    private final TreeMap<String,RootVariable> variables = new TreeMap<>();

    public FakeEnvironment(ParsingEnvironment environment) {
        this.environment = environment;
    }


    @Override
    public RootVariable declareRootVariable(String name, Type type, boolean constant) {
        RootVariable newVar = new RootVariable();
        newVar.name = name;
        newVar.type = type;
        newVar.constant = constant;
        variables.put(name, newVar);
        return newVar;
    }

    @Override
    public RootVariable getRootVariable(String name) {
        RootVariable result = variables.get(name);
        return result == null ? environment.getRootVariable(name) : result;
    }

    @Override
    public InstanceType resolveInstanceType(String typeName) {
        return environment.resolveInstanceType(typeName);
    }
}
