package org.kobjects.codechat.statement.unresolved;

import org.kobjects.codechat.annotation.AnnotatedStringBuilder;
import org.kobjects.codechat.expr.Expression;
import org.kobjects.codechat.expr.unresolved.UnresolvedExpression;
import org.kobjects.codechat.lang.RootVariable;
import org.kobjects.codechat.lang.UserMethod;
import org.kobjects.codechat.parser.ParsingContext;
import org.kobjects.codechat.statement.ClassDeclaration;
import org.kobjects.codechat.statement.Statement;
import org.kobjects.codechat.type.UserClassType;

import java.util.ArrayList;
import org.kobjects.codechat.type.unresolved.UnresolvedFunctionSignature;

public class UnresolvedClassDeclaration extends UnresolvedStatement {
  private ArrayList<UnresolvedField> fields = new ArrayList<>();
  private ArrayList<UnresolvedMethod> methods = new ArrayList<>();
  private RootVariable variable;
  private String className;
  private UserClassType type;

  public UnresolvedClassDeclaration(String className) {
    this.className = className;
  }

  @Override
  public void toString(AnnotatedStringBuilder asb, int indent) {
    asb.append("class ").append(className).append(":\n");
    for (UnresolvedField field: fields) {
      field.toString(asb, indent + 2);
    }
    for (UnresolvedMethod method: methods) {
      method.toString(asb, indent + 2);
    }
    asb.indent(indent);
    asb.append("end\n");
  }

  public void addField(UnresolvedField unresolvedField) {
    fields.add(unresolvedField);
  }

  public void addMethod(UnresolvedMethod unresolvedMethod) {
    methods.add(unresolvedMethod);
  }

  @Override
  public Statement resolve(ParsingContext parsingContext) {

    int index = 0;
    for (UnresolvedField field: fields) {
      Expression resolvedInitializer = field.initializer.resolve(parsingContext, null);
      type.addProperty(index++, field.name, resolvedInitializer.getType(), true, null, resolvedInitializer);
    }

    for (UnresolvedMethod method: methods) {
      ParsingContext methodParsingContext = new ParsingContext(parsingContext, type);
      for (int i = 0; i < method.signature.parameterNames.size(); i++) {
        methodParsingContext.addVariable(method.signature.parameterNames.get(i), method.signature.parameterTypes.get(i).resolve(parsingContext), true);
      }
      type.addMethod(new UserMethod(method.name, method.signature.resolve(parsingContext), method.signature.getParemeterNameArray(), method.body.resolve(methodParsingContext)));
    }

    return new ClassDeclaration(variable, type);
  }

  @Override
  public void resolveTypes(ParsingContext parsingContext) {
    type = new UserClassType(parsingContext.environment.getEnvironment());
    variable = parsingContext.environment.declareRootVariable(className, type.getType(), true);
    variable.value = type;
  }

  public static class UnresolvedField {
    private final UnresolvedExpression initializer;
    private final String name;

    public UnresolvedField(String name, UnresolvedExpression initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    public void toString(AnnotatedStringBuilder asb, int indent) {
      asb.indent(indent);
      asb.append(name);
      asb.append(" = ");
      initializer.toString(asb, indent + 4);
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
