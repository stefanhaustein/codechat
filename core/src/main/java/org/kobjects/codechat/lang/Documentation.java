package org.kobjects.codechat.lang;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.annotation.DocumentedLink;
import org.kobjects.codechat.annotation.Title;
import org.kobjects.codechat.function.Function;
import org.kobjects.codechat.type.FunctionType;
import org.kobjects.codechat.type.Classifier;
import org.kobjects.codechat.type.MetaType;
import org.kobjects.codechat.type.Type;

public class Documentation {

    public static CharSequence getDocumentation(Environment environment, Object object) {
        AnnotatedStringBuilder asb = new AnnotatedStringBuilder();
        printDocumentation(asb, object, environment);
        return asb.build();
    }


    public static void printDocumentation(AnnotatedStringBuilder asb, Object object, Environment environment) {
        String constantName = environment.constants.get(object);
        if (constantName != null) {
            RootVariable var = environment.getRootVariable(constantName);
            if (var.value == object) {
                printDocumentation(asb, var);
                return;
            }
        }

        if (object instanceof RootVariable) {
            printDocumentation(asb, (RootVariable) object);
            return;
        }

        asb.append("No documentation available for " + object);
    }


    public static void printDocumentation(AnnotatedStringBuilder asb, RootVariable variable) {
        if (variable.value instanceof Function) {
            FunctionType type = (FunctionType) variable.type;
            asb.append(variable.name + (type.parameterTypes.length == 0 ? "()" : "(...)")+ "\n\n", new Title());

            if (type.parameterTypes.length > 0) {
                asb.append("Parameter types\n");
                for (Type paramType : type.parameterTypes) {
                    asb.append(" - ");
                    asb.append(paramType.getName(), new DocumentedLink(paramType));
                    asb.append("\n");
                }
                asb.append("\n");
            }

            if (type.returnType != null) {
                asb.append("Return type: ");
                asb.append(type.returnType.getName(), new DocumentedLink(type.returnType));
                asb.append("\n");
                asb.append("\n");
            }
        } else if (variable.type instanceof Classifier && !((Classifier) variable.type).isInstantiable()) {
            asb.append(variable.name + "\n\n", new Title());
        } else if (!(variable.type instanceof MetaType)) {
            asb.append(variable.constant ? "constant ": "variable ");
            asb.append(variable.name).append(": ");
            asb.append(variable.type.getName(), new DocumentedLink(variable.type));
            if (variable.constant) {
                asb.append(" = ");
                Formatting.toLiteral(asb, variable.value);
            }
            asb.append("\n");
        }

        if (variable.value instanceof HasDocumentationDetail) {
            ((HasDocumentationDetail) variable.value).printDocumentationDetail(asb);
        }

        if (variable.documentation != null) {
            asb.append(variable.documentation);
            asb.append("\n");
        }
    }

    /*
    public final void printDocumentation(AnnotatedStringBuilder asb) {
        printDocumentationBody(asb);
        asb.append("\n\nProperties:\n");
        boolean first = true;
        for (Classifier.PropertyDescriptor propertyDescriptor: properties()) {
            asb.append("\n- ");
            asb.append(propertyDescriptor.name, new DocumentedLink(propertyDescriptor));
        }
    }
    */

}
