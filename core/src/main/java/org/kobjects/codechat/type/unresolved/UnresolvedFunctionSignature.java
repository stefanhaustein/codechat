package org.kobjects.codechat.type.unresolved;

import java.util.ArrayList;
import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.lang.Environment;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Type;

public class UnresolvedFunctionSignature implements UnresolvedType {
    public ArrayList<String> parameterNames = new ArrayList<>();
    public ArrayList<UnresolvedType> parameterTypes = new ArrayList<>();
    public UnresolvedType returnType;


    @Override
    public FunctionType resolve(Environment environment) {
        Type[] resolvedParameterTypes = new Type[parameterTypes.size()];
        Type resolvedReturnType = returnType == null ? null : returnType.resolve(environment);
        for (int i = 0; i < resolvedParameterTypes.length; i++) {
            resolvedParameterTypes[i] = parameterTypes.get(i).resolve(environment);
        }
        return new FunctionType(resolvedReturnType, resolvedParameterTypes);
    }

    @Override
    public FunctionType resolve(ParsingContext parsingContext) {
        return resolve(parsingContext.environment.getEnvironment());
    }

    public void print(AnnotatedStringBuilder asb) {
        asb.append('(');
        for (int i = 0; i < parameterNames.size(); i++) {
            if (i != 0) {
                asb.append(", ");
            }
            asb.append(parameterNames.get(i));
            asb.append(": ");
            parameterTypes.get(i).print(asb);
        }
        asb.append(')');
        if (returnType != null) {
            asb.append(" -> ");
            returnType.print(asb);
        }
    }

    public String[] getParemeterNameArray() {
        return parameterNames.toArray(new String[parameterNames.size()]);
    }
}
