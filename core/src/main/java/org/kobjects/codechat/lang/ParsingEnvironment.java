package org.kobjects.codechat.lang;

import org.kobjects.codechat.type.InstanceType;
import org.kobjects.codechat.type.Type; /**
 * Parts of the environment relevant for parsing. Referenced in ParsingContext. Implemented by Environment and
 * ShadowEnvironment.
 */
public interface ParsingEnvironment {
    RootVariable declareRootVariable(String variableName, Type resolvedType, boolean constant);

    Type resolveType(String typeName);

    RootVariable getRootVariable(String name);

    InstanceType resolveInstanceType(String typeName);

    <T extends Instance> T getInstance(InstanceType<T> type, int id, boolean force);
}
