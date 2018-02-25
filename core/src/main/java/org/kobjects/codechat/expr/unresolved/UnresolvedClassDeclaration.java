package org.kobjects.codechat.expr.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.instance.UserMethod;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.expr.ClassDeclaration;
import org.kobjects.codechat.statement.unresolved.UnresolvedStatement;
import org.kobjects.codechat.type.Type;
import org.kobjects.codechat.type.UserClassifier;

import java.util.ArrayList;
import org.kobjects.codechat.type.unresolved.UnresolvedFunctionSignature;
import org.kobjects.codechat.type.unresolved.UnresolvedType;

public class UnresolvedClassDeclaration extends UnresolvedExpression {
  private ArrayList<UnresolvedField> fields = new ArrayList<>();
  private ArrayList<UnresolvedMethod> methods = new ArrayList<>();

  public UnresolvedClassDeclaration(int start, int end) {
    super(start, end);
  }


  @Override
  public void toString(AnnotatedStringBuilder asb, int indent) {
    asb.append("class:\n");
    printBody(asb, indent);
    asb.indent(indent);
    asb.append("end\n");
  }

  public void printBody(AnnotatedStringBuilder asb, int indent) {
    for (UnresolvedField field: fields) {
      field.toString(asb, indent + 2);
    }
    for (UnresolvedMethod method: methods) {
      method.toString(asb, indent + 2);
    }
  }

  @Override
  public int getPrecedence() {
    return 0;
  }

  public void addField(UnresolvedField unresolvedField) {
    fields.add(unresolvedField);
  }

  public void addMethod(UnresolvedMethod unresolvedMethod) {
    methods.add(unresolvedMethod);
  }

  @Override
  public Expression resolve(final ParsingContext parsingContext, Type expectedType) {
    final UserClassifier type = new UserClassifier(parsingContext.environment.getEnvironment());

    parsingContext.enqueue(new Runnable() {
      @Override
      public void run() {
        int index = 0;
        for (UnresolvedField field: fields) {
          Type resolvedType = field.type == null ? null : field.type.resolve(parsingContext.environment.getEnvironment());
          Expression resolvedInitializer = field.initializer == null ? null : field.initializer.resolve(parsingContext, null);
          type.addProperty(index++, field.name, resolvedType, true, null, resolvedInitializer);
        }

        for (final UnresolvedMethod method: methods) {
          final UserMethod resolved = new UserMethod(method.name, method.signature.resolve(parsingContext), method.signature.getParemeterNameArray());
          parsingContext.enqueue(new Runnable() {
            @Override
            public void run() {
              ParsingContext methodParsingContext = new ParsingContext(parsingContext, type);
              for (int i = 0; i < method.signature.parameterNames.size(); i++) {
                methodParsingContext.addVariable(method.signature.parameterNames.get(i), method.signature.parameterTypes.get(i).resolve(parsingContext), true);
              }
              resolved.setBody(method.body.resolve(methodParsingContext));
            }
          });

          type.addMethod(resolved);
        }
      }
    });

    return new ClassDeclaration(type);
  }

  public static class UnresolvedField {
    private final UnresolvedType type;
    private final UnresolvedExpression initializer;
    private final String name;

    public UnresolvedField(String name, UnresolvedType type, UnresolvedExpression initializer) {
      this.name = name;
      this.type = type;
      this.initializer = initializer;
    }

    public void toString(AnnotatedStringBuilder asb, int indent) {
      asb.indent(indent);
      asb.append(name);
      if (type != null) {
        asb.append(": ");
        asb.append(type.toString());
        if (initializer != null) {
          asb.append(" = ");
          initializer.toString(asb, indent + 4);
        }
      } else {
        asb.append(" := ");
        initializer.toString(asb, indent + 4);
      }
    }
  }

  public static class UnresolvedMethod {
    private final String name;
    private final UnresolvedFunctionSignature signature;
    private final UnresolvedStatement body;

    public UnresolvedMethod(String name, UnresolvedFunctionSignature signature, UnresolvedStatement body) {
      this.name = name;
      this.signature = signature;
      this.body = body;
    }

    public void toString(AnnotatedStringBuilder asb, int indent) {
      asb.indent(indent);
      signature.print(asb);
      asb.append(":\n");
      body.toString(asb, indent + 2);
    }
  }
}
