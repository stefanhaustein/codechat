package org.kobjects.codechat.parser;

import org.kobjects.codechat.lang.Instance;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type;

/**
 * Parts of the environment relevant for parsing. Referenced in ParsingContext. Implemented by Environment and
 * ShadowEnvironment.
 */
public interface ParsingEnvironment {
    RootVariable declareRootVariable(String variableName, Type resolvedType, boolean constant);

    RootVariable getRootVariable(String name);

    InstanceType resolveInstanceType(String typeName);

    String getConstantName(Instance value);

  void removeVariable(String name);
}
